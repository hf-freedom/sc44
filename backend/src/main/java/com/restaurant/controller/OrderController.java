package com.restaurant.controller;

import com.restaurant.model.ApiResponse;
import com.restaurant.model.Order;
import com.restaurant.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ApiResponse<List<Order>> getAllOrders() {
        return ApiResponse.success(orderService.getAllOrders());
    }

    @GetMapping("/today")
    public ApiResponse<List<Order>> getTodayOrders() {
        return ApiResponse.success(orderService.getTodayOrders());
    }

    @GetMapping("/{id}")
    public ApiResponse<Order> getOrderById(@PathVariable String id) {
        Order order = orderService.getOrderById(id);
        if (order == null) {
            return ApiResponse.error("订单不存在");
        }
        return ApiResponse.success(order);
    }

    @PostMapping
    public ApiResponse<Order> createOrder(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemsMap = (List<Map<String, Object>>) request.get("items");
            String remark = (String) request.getOrDefault("remark", "");

            List<OrderService.OrderItemRequest> items = new java.util.ArrayList<>();
            for (Map<String, Object> itemMap : itemsMap) {
                OrderService.OrderItemRequest item = new OrderService.OrderItemRequest();
                item.setDishId((String) itemMap.get("dishId"));
                item.setQuantity(((Number) itemMap.getOrDefault("quantity", 1)).intValue());
                items.add(item);
            }

            Order order = orderService.createOrder(items, remark);
            return ApiResponse.success("订单创建成功", order);
        } catch (Exception e) {
            return ApiResponse.error("订单创建失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/pay")
    public ApiResponse<Order> payOrder(@PathVariable String id) {
        try {
            Order order = orderService.payOrder(id);
            return ApiResponse.success("支付成功", order);
        } catch (Exception e) {
            return ApiResponse.error("支付失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/deliver")
    public ApiResponse<Order> deliverOrder(@PathVariable String id) {
        try {
            Order order = orderService.deliverOrder(id);
            return ApiResponse.success("出餐成功", order);
        } catch (Exception e) {
            return ApiResponse.error("出餐失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Order> cancelOrder(@PathVariable String id) {
        try {
            Order order = orderService.cancelOrder(id);
            return ApiResponse.success("取消成功", order);
        } catch (Exception e) {
            return ApiResponse.error("取消失败: " + e.getMessage());
        }
    }

    @PostMapping("/{orderId}/items/{itemId}/refund")
    public ApiResponse<OrderService.OrderItemRefundResult> refundOrderItem(
            @PathVariable String orderId,
            @PathVariable String itemId,
            @RequestBody Map<String, Object> request) {
        try {
            int quantity = ((Number) request.getOrDefault("quantity", 1)).intValue();
            String reason = (String) request.getOrDefault("reason", "");

            OrderService.OrderItemRefundResult result = orderService.refundOrderItem(orderId, itemId, quantity, reason);
            return ApiResponse.success(result.getMessage(), result);
        } catch (Exception e) {
            return ApiResponse.error("退款失败: " + e.getMessage());
        }
    }
}