package com.restaurant.service;

import com.restaurant.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PurchaseService {

    @Autowired
    private DataStorageService dataStorageService;

    @Autowired
    private IngredientService ingredientService;

    @Autowired
    private DishService dishService;

    public List<PurchaseOrder> getAllPurchaseOrders() {
        List<PurchaseOrder> orders = new ArrayList<>(dataStorageService.getPurchaseOrders().values());
        orders.sort((o1, o2) -> {
            if (o1.getCreateTime() == null) return 1;
            if (o2.getCreateTime() == null) return -1;
            return o2.getCreateTime().compareTo(o1.getCreateTime());
        });
        return orders;
    }

    public PurchaseOrder getPurchaseOrderById(String id) {
        return dataStorageService.getPurchaseOrders().get(id);
    }

    public PurchaseOrder createPurchaseOrder(List<PurchaseOrderItem> items, String remark) {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(UUID.randomUUID().toString());
        order.setOrderNo(dataStorageService.generatePurchaseNo());
        order.setCreateTime(LocalDateTime.now());
        order.setStatus(PurchaseOrderStatus.PENDING);
        order.setRemark(remark);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseOrderItem item : items) {
            item.setId(UUID.randomUUID().toString());
            if (item.getAmount() == null && item.getUnitPrice() != null && item.getQuantity() != null) {
                item.setAmount(item.getUnitPrice().multiply(item.getQuantity()));
            }
            if (item.getAmount() != null) {
                totalAmount = totalAmount.add(item.getAmount());
            }
        }

        order.setItems(items);
        order.setTotalAmount(totalAmount);

        dataStorageService.getPurchaseOrders().put(order.getId(), order);
        return order;
    }

    public PurchaseOrder createFromSuggestions(List<String> suggestionIds) {
        List<PurchaseOrderItem> items = new ArrayList<>();

        for (String suggestionId : suggestionIds) {
            PurchaseSuggestion suggestion = dataStorageService.getPurchaseSuggestions().get(suggestionId);
            if (suggestion == null || suggestion.isConverted()) {
                continue;
            }

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setId(UUID.randomUUID().toString());
            item.setIngredientId(suggestion.getIngredientId());
            item.setIngredientName(suggestion.getIngredientName());
            item.setUnit(suggestion.getUnit());
            item.setSafeInventory(suggestion.getSafeInventory());
            item.setCurrentInventory(suggestion.getCurrentInventory());
            item.setQuantity(suggestion.getSuggestedQuantity());

            Ingredient ingredient = ingredientService.getIngredientById(suggestion.getIngredientId());
            if (ingredient != null && ingredient.getUnitPrice() != null) {
                item.setUnitPrice(ingredient.getUnitPrice());
                item.setAmount(ingredient.getUnitPrice().multiply(suggestion.getSuggestedQuantity()));
            }

            items.add(item);

            suggestion.setConverted(true);
            suggestion.setConvertTime(LocalDateTime.now());
        }

        if (items.isEmpty()) {
            throw new RuntimeException("没有有效的采购建议");
        }

        PurchaseOrder order = createPurchaseOrder(items, "由采购建议生成");
        for (PurchaseOrderItem item : items) {
            for (String suggestionId : suggestionIds) {
                PurchaseSuggestion suggestion = dataStorageService.getPurchaseSuggestions().get(suggestionId);
                if (suggestion != null && suggestion.getIngredientId().equals(item.getIngredientId())) {
                    suggestion.setConvertedPurchaseOrderId(order.getId());
                }
            }
        }
        return order;
    }

    public PurchaseOrder receivePurchaseOrder(String orderId) {
        PurchaseOrder order = getPurchaseOrderById(orderId);
        if (order == null) {
            throw new RuntimeException("采购单不存在");
        }
        if (order.getStatus() != PurchaseOrderStatus.PENDING) {
            throw new RuntimeException("采购单状态不正确");
        }

        for (PurchaseOrderItem item : order.getItems()) {
            if (item.getQuantity() != null) {
                ingredientService.updateInventory(item.getIngredientId(), item.getQuantity());

                Ingredient ingredient = ingredientService.getIngredientById(item.getIngredientId());
                if (ingredient != null && item.getUnitPrice() != null) {
                    ingredient.setUnitPrice(item.getUnitPrice());
                }
            }
        }

        order.setStatus(PurchaseOrderStatus.RECEIVED);
        order.setReceiveTime(LocalDateTime.now());

        dishService.updateDishAvailability();
        return order;
    }

    public List<PurchaseSuggestion> getAllPurchaseSuggestions() {
        List<PurchaseSuggestion> suggestions = new ArrayList<>(dataStorageService.getPurchaseSuggestions().values());
        suggestions.sort((s1, s2) -> {
            if (s1.getCreateTime() == null) return 1;
            if (s2.getCreateTime() == null) return -1;
            return s2.getCreateTime().compareTo(s1.getCreateTime());
        });
        return suggestions;
    }

    public PurchaseSuggestion createPurchaseSuggestion(Ingredient ingredient) {
        if (ingredient == null) return null;

        PurchaseSuggestion suggestion = new PurchaseSuggestion();
        suggestion.setId(UUID.randomUUID().toString());
        suggestion.setIngredientId(ingredient.getId());
        suggestion.setIngredientName(ingredient.getName());
        suggestion.setCurrentInventory(ingredient.getInventory());
        suggestion.setSafeInventory(ingredient.getSafeInventory());
        suggestion.setUnit(ingredient.getUnit());

        BigDecimal suggestQty;
        if (ingredient.getSafeInventory() != null && ingredient.getInventory() != null) {
            suggestQty = ingredient.getSafeInventory().multiply(new BigDecimal("2")).subtract(ingredient.getInventory());
            if (suggestQty.compareTo(BigDecimal.ZERO) <= 0) {
                suggestQty = ingredient.getSafeInventory();
            }
        } else {
            suggestQty = new BigDecimal("10");
        }
        suggestion.setSuggestedQuantity(suggestQty);

        dataStorageService.getPurchaseSuggestions().put(suggestion.getId(), suggestion);
        return suggestion;
    }

    public List<PurchaseSuggestion> generatePurchaseSuggestions() {
        List<PurchaseSuggestion> result = new ArrayList<>();
        List<Ingredient> lowInventoryIngredients = ingredientService.getLowInventoryIngredients();
        
        if (lowInventoryIngredients.isEmpty()) {
            return result;
        }

        List<PurchaseSuggestion> existingSuggestions = getAllPurchaseSuggestions();
        
        for (Ingredient ingredient : lowInventoryIngredients) {
            boolean hasUnconvertedSuggestion = false;
            for (PurchaseSuggestion suggestion : existingSuggestions) {
                if (suggestion.getIngredientId().equals(ingredient.getId()) && !suggestion.isConverted()) {
                    hasUnconvertedSuggestion = true;
                    break;
                }
            }
            if (!hasUnconvertedSuggestion) {
                PurchaseSuggestion newSuggestion = createPurchaseSuggestion(ingredient);
                result.add(newSuggestion);
            }
        }
        
        return result;
    }
}