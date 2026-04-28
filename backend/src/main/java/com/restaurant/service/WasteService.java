package com.restaurant.service;

import com.restaurant.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class WasteService {

    @Autowired
    private DataStorageService dataStorageService;

    @Autowired
    private IngredientService ingredientService;

    @Autowired
    private DishService dishService;

    public List<WasteOrder> getAllWasteOrders() {
        List<WasteOrder> orders = new ArrayList<>(dataStorageService.getWasteOrders().values());
        orders.sort((o1, o2) -> {
            if (o1.getCreateTime() == null) return 1;
            if (o2.getCreateTime() == null) return -1;
            return o2.getCreateTime().compareTo(o1.getCreateTime());
        });
        return orders;
    }

    public WasteOrder getWasteOrderById(String id) {
        return dataStorageService.getWasteOrders().get(id);
    }

    public WasteOrder createWasteOrder(WasteReason reason, List<WasteOrderItem> items, String remark) {
        WasteOrder order = new WasteOrder();
        order.setId(UUID.randomUUID().toString());
        order.setOrderNo(dataStorageService.generateWasteNo());
        order.setReason(reason);
        order.setRemark(remark);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (WasteOrderItem item : items) {
            item.setId(UUID.randomUUID().toString());

            Ingredient ingredient = ingredientService.getIngredientById(item.getIngredientId());
            if (ingredient != null) {
                if (item.getIngredientName() == null) {
                    item.setIngredientName(ingredient.getName());
                }
                if (item.getUnit() == null) {
                    item.setUnit(ingredient.getUnit());
                }
                if (item.getUnitPrice() == null && ingredient.getUnitPrice() != null) {
                    item.setUnitPrice(ingredient.getUnitPrice());
                }
            }

            if (item.getUnitPrice() != null && item.getQuantity() != null) {
                item.setAmount(item.getUnitPrice().multiply(item.getQuantity()));
            }

            if (item.getAmount() != null) {
                totalAmount = totalAmount.add(item.getAmount());
            }
        }

        order.setItems(items);
        order.setTotalAmount(totalAmount);

        for (WasteOrderItem item : items) {
            if (item.getQuantity() != null) {
                ingredientService.updateInventory(item.getIngredientId(), item.getQuantity().negate());
            }
        }

        dataStorageService.getWasteOrders().put(order.getId(), order);
        dishService.updateDishAvailability();
        return order;
    }

    public WasteOrder createExpiredWasteOrder() {
        List<Ingredient> expiredIngredients = ingredientService.getExpiredIngredients();
        if (expiredIngredients.isEmpty()) {
            return null;
        }

        List<WasteOrderItem> items = new ArrayList<>();
        for (Ingredient ingredient : expiredIngredients) {
            if (ingredient.getInventory() == null || ingredient.getInventory().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            WasteOrderItem item = new WasteOrderItem();
            item.setIngredientId(ingredient.getId());
            item.setIngredientName(ingredient.getName());
            item.setQuantity(ingredient.getInventory());
            item.setUnit(ingredient.getUnit());
            item.setUnitPrice(ingredient.getUnitPrice());

            if (item.getUnitPrice() != null) {
                item.setAmount(item.getUnitPrice().multiply(item.getQuantity()));
            }

            items.add(item);
        }

        if (items.isEmpty()) {
            return null;
        }

        return createWasteOrder(WasteReason.EXPIRED, items, "系统自动生成-原材料过期损耗");
    }

    public List<WasteOrder> getTodayWasteOrders() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        List<WasteOrder> result = new ArrayList<>();
        for (WasteOrder order : dataStorageService.getWasteOrders().values()) {
            if (order.getCreateTime() != null && order.getCreateTime().isAfter(startOfDay)) {
                result.add(order);
            }
        }
        result.sort((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()));
        return result;
    }

    public BigDecimal getTodayWasteAmount() {
        List<WasteOrder> todayOrders = getTodayWasteOrders();
        BigDecimal total = BigDecimal.ZERO;
        for (WasteOrder order : todayOrders) {
            if (order.getTotalAmount() != null) {
                total = total.add(order.getTotalAmount());
            }
        }
        return total;
    }
}