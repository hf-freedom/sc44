package com.restaurant.controller;

import com.restaurant.model.ApiResponse;
import com.restaurant.model.WasteOrder;
import com.restaurant.model.WasteReason;
import com.restaurant.service.WasteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wastes")
public class WasteController {

    @Autowired
    private WasteService wasteService;

    @GetMapping
    public ApiResponse<List<WasteOrder>> getAllWasteOrders() {
        return ApiResponse.success(wasteService.getAllWasteOrders());
    }

    @GetMapping("/today")
    public ApiResponse<List<WasteOrder>> getTodayWasteOrders() {
        return ApiResponse.success(wasteService.getTodayWasteOrders());
    }

    @GetMapping("/{id}")
    public ApiResponse<WasteOrder> getWasteOrderById(@PathVariable String id) {
        WasteOrder order = wasteService.getWasteOrderById(id);
        if (order == null) {
            return ApiResponse.error("损耗单不存在");
        }
        return ApiResponse.success(order);
    }

    @PostMapping
    public ApiResponse<WasteOrder> createWasteOrder(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemsMap = (List<Map<String, Object>>) request.get("items");
            String reasonStr = (String) request.getOrDefault("reason", "OTHER");
            String remark = (String) request.getOrDefault("remark", "");

            WasteReason reason;
            try {
                reason = WasteReason.valueOf(reasonStr.toUpperCase());
            } catch (Exception e) {
                reason = WasteReason.OTHER;
            }

            List<com.restaurant.model.WasteOrderItem> items = new java.util.ArrayList<>();
            for (Map<String, Object> itemMap : itemsMap) {
                com.restaurant.model.WasteOrderItem item = new com.restaurant.model.WasteOrderItem();
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

            WasteOrder order = wasteService.createWasteOrder(reason, items, remark);
            return ApiResponse.success("损耗单创建成功", order);
        } catch (Exception e) {
            return ApiResponse.error("损耗单创建失败: " + e.getMessage());
        }
    }
}