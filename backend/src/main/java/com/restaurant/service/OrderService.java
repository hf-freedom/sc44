package com.restaurant.service;

import com.restaurant.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private DataStorageService dataStorageService;

    @Autowired
    private DishService dishService;

    @Autowired
    private IngredientService ingredientService;

    @Autowired
    private SettlementService settlementService;

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>(dataStorageService.getOrders().values());
        orders.sort((o1, o2) -> {
            if (o1.getCreateTime() == null) return 1;
            if (o2.getCreateTime() == null) return -1;
            return o2.getCreateTime().compareTo(o1.getCreateTime());
        });
        return orders;
    }

    public Order getOrderById(String id) {
        return dataStorageService.getOrders().get(id);
    }

    public Order createOrder(List<OrderItemRequest> items, String remark) {
        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setOrderNo(dataStorageService.generateOrderNo());
        order.setCreateTime(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setRemark(remark);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest request : items) {
            Dish dish = dishService.getDishById(request.getDishId());
            if (dish == null) {
                throw new RuntimeException("菜品不存在: " + request.getDishId());
            }
            if (dish.getStatus() != DishStatus.AVAILABLE) {
                throw new RuntimeException("菜品不可售: " + dish.getName());
            }
            if (!dishService.canMakeDish(dish, request.getQuantity())) {
                throw new RuntimeException("原材料不足，无法制作: " + dish.getName());
            }

            OrderItem item = new OrderItem();
            item.setId(UUID.randomUUID().toString());
            item.setDishId(dish.getId());
            item.setDishName(dish.getName());
            item.setPrice(dish.getPrice());
            item.setQuantity(request.getQuantity());
            item.setAmount(dish.getPrice().multiply(new BigDecimal(request.getQuantity())));
            item.setRecipeSnapshot(new ArrayList<>(dish.getRecipe()));

            orderItems.add(item);
            totalAmount = totalAmount.add(item.getAmount());
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        dataStorageService.getOrders().put(order.getId(), order);
        return order;
    }

    public Order payOrder(String orderId) {
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("订单状态不正确");
        }

        for (OrderItem item : order.getItems()) {
            Dish dish = dishService.getDishById(item.getDishId());
            if (dish != null) {
                dishService.lockIngredientsForDish(dish, item.getQuantity());
            }
        }

        order.setStatus(OrderStatus.PAID);
        order.setPaymentTime(LocalDateTime.now());
        order.setPaidAmount(order.getTotalAmount());

        dishService.updateDishAvailability();
        return order;
    }

    public Order deliverOrder(String orderId) {
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != OrderStatus.PAID) {
            throw new RuntimeException("订单状态不正确");
        }

        for (OrderItem item : order.getItems()) {
            Dish dish = dishService.getDishById(item.getDishId());
            if (dish != null) {
                dishService.consumeIngredientsForDish(dish, item.getQuantity());
            }
        }

        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveryTime(LocalDateTime.now());

        dishService.updateDishAvailability();
        return order;
    }

    public Order cancelOrder(String orderId) {
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PAID) {
            throw new RuntimeException("订单状态不正确");
        }

        if (order.getStatus() == OrderStatus.PAID) {
            for (OrderItem item : order.getItems()) {
                Dish dish = dishService.getDishById(item.getDishId());
                if (dish != null) {
                    dishService.unlockIngredientsForDish(dish, item.getQuantity());
                }
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelTime(LocalDateTime.now());

        dishService.updateDishAvailability();
        return order;
    }

    public OrderItemRefundResult refundOrderItem(String orderId, String orderItemId, int quantity, String reason) {
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != OrderStatus.DELIVERED && order.getStatus() != OrderStatus.REFUNDED) {
            throw new RuntimeException("订单状态不正确");
        }
        if (order.isSettled()) {
            SettlementAdjustment adjustment = settlementService.createRefundAdjustment(order, reason);
            OrderItemRefundResult result = new OrderItemRefundResult();
            result.setRefunded(false);
            result.setAdjustment(adjustment);
            result.setMessage("订单已日结，已生成调整单");
            return result;
        }

        OrderItem orderItem = null;
        for (OrderItem item : order.getItems()) {
            if (item.getId().equals(orderItemId)) {
                orderItem = item;
                break;
            }
        }

        if (orderItem == null) {
            throw new RuntimeException("订单项不存在");
        }

        int availableForRefund = orderItem.getQuantity() - orderItem.getRefundedQuantity();
        if (quantity > availableForRefund) {
            throw new RuntimeException("退款数量超过可退数量");
        }

        orderItem.setRefundedQuantity(orderItem.getRefundedQuantity() + quantity);
        if (orderItem.getRefundedQuantity() == orderItem.getQuantity()) {
            orderItem.setRefunded(true);
        }

        BigDecimal refundAmount = orderItem.getPrice().multiply(new BigDecimal(quantity));
        order.setRefundAmount(order.getRefundAmount().add(refundAmount));

        boolean allRefunded = order.getItems().stream().allMatch(OrderItem::isRefunded);
        if (allRefunded) {
            order.setStatus(OrderStatus.REFUNDED);
        }

        OrderItemRefundResult result = new OrderItemRefundResult();
        result.setRefunded(true);
        result.setRefundAmount(refundAmount);
        result.setOrder(order);
        result.setMessage("退款成功");

        return result;
    }

    public List<Order> getTodayOrders() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return dataStorageService.getOrders().values().stream()
                .filter(o -> o.getCreateTime() != null && o.getCreateTime().isAfter(startOfDay))
                .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                .collect(Collectors.toList());
    }

    public static class OrderItemRequest {
        private String dishId;
        private int quantity;

        public String getDishId() {
            return dishId;
        }

        public void setDishId(String dishId) {
            this.dishId = dishId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    public static class OrderItemRefundResult {
        private boolean refunded;
        private Order order;
        private SettlementAdjustment adjustment;
        private BigDecimal refundAmount;
        private String message;

        public boolean isRefunded() {
            return refunded;
        }

        public void setRefunded(boolean refunded) {
            this.refunded = refunded;
        }

        public Order getOrder() {
            return order;
        }

        public void setOrder(Order order) {
            this.order = order;
        }

        public SettlementAdjustment getAdjustment() {
            return adjustment;
        }

        public void setAdjustment(SettlementAdjustment adjustment) {
            this.adjustment = adjustment;
        }

        public BigDecimal getRefundAmount() {
            return refundAmount;
        }

        public void setRefundAmount(BigDecimal refundAmount) {
            this.refundAmount = refundAmount;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}