package com.restaurant.controller;

import com.restaurant.model.ApiResponse;
import com.restaurant.model.PurchaseOrder;
import com.restaurant.model.PurchaseSuggestion;
import com.restaurant.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;

    @GetMapping
    public ApiResponse<List<PurchaseOrder>> getAllPurchaseOrders() {
        return ApiResponse.success(purchaseService.getAllPurchaseOrders());
    }

    @GetMapping("/{id}")
    public ApiResponse<PurchaseOrder> getPurchaseOrderById(@PathVariable String id) {
        PurchaseOrder order = purchaseService.getPurchaseOrderById(id);
        if (order == null) {
            return ApiResponse.error("采购单不存在");
        }
        return ApiResponse.success(order);
    }

    @PostMapping
    public ApiResponse<PurchaseOrder> createPurchaseOrder(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemsMap = (List<Map<String, Object>>) request.get("items");
            String remark = (String) request.getOrDefault("remark", "");

            List<com.restaurant.model.PurchaseOrderItem> items = new java.util.ArrayList<>();
            for (Map<String, Object> itemMap : itemsMap) {
                com.restaurant.model.PurchaseOrderItem item = new com.restaurant.model.PurchaseOrderItem();
                item.setIngredientId((String) itemMap.get("ingredientId"));
                item.setIngredientName((String) itemMap.get("ingredientName"));
                
                Object quantityObj = itemMap.get("quantity");
                if (quantityObj != null) {
                    if (quantityObj instanceof Number) {
                        item.setQuantity(new java.math.BigDecimal(((Number) quantityObj).doubleValue()));
                    } else if (quantityObj instanceof String) {
                        item.setQuantity(new java.math.BigDecimal((String) quantityObj));
                    }
                }
                
                Object unitPriceObj = itemMap.get("unitPrice");
                if (unitPriceObj != null) {
                    if (unitPriceObj instanceof Number) {
                        item.setUnitPrice(new java.math.BigDecimal(((Number) unitPriceObj).doubleValue()));
                    } else if (unitPriceObj instanceof String) {
                        item.setUnitPrice(new java.math.BigDecimal((String) unitPriceObj));
                    }
                }
                
                item.setUnit((String) itemMap.get("unit"));
                items.add(item);
            }

            PurchaseOrder order = purchaseService.createPurchaseOrder(items, remark);
            return ApiResponse.success("采购单创建成功", order);
        } catch (Exception e) {
            return ApiResponse.error("采购单创建失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/receive")
    public ApiResponse<PurchaseOrder> receivePurchaseOrder(@PathVariable String id) {
        try {
            PurchaseOrder order = purchaseService.receivePurchaseOrder(id);
            return ApiResponse.success("采购到货成功", order);
        } catch (Exception e) {
            return ApiResponse.error("采购到货失败: " + e.getMessage());
        }
    }

    @GetMapping("/suggestions")
    public ApiResponse<List<PurchaseSuggestion>> getAllPurchaseSuggestions() {
        return ApiResponse.success(purchaseService.getAllPurchaseSuggestions());
    }

    @PostMapping("/suggestions/generate")
    public ApiResponse<List<PurchaseSuggestion>> generatePurchaseSuggestions() {
        try {
            List<PurchaseSuggestion> suggestions = purchaseService.generatePurchaseSuggestions();
            if (suggestions.isEmpty()) {
                return ApiResponse.success("所有原材料库存充足，无需生成采购建议", suggestions);
            }
            return ApiResponse.success("生成了 " + suggestions.size() + " 条采购建议", suggestions);
        } catch (Exception e) {
            return ApiResponse.error("生成采购建议失败: " + e.getMessage());
        }
    }

    @PostMapping("/suggestions/convert")
    public ApiResponse<PurchaseOrder> convertFromSuggestions(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<String> suggestionIds = (List<String>) request.get("suggestionIds");
            
            if (suggestionIds == null || suggestionIds.isEmpty()) {
                return ApiResponse.error("请选择要转换的采购建议");
            }
            
            PurchaseOrder order = purchaseService.createFromSuggestions(suggestionIds);
            return ApiResponse.success("采购建议转换成功", order);
        } catch (Exception e) {
            return ApiResponse.error("采购建议转换失败: " + e.getMessage());
        }
    }
}