package com.restaurant.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderItem {
    private String id;
    private String dishId;
    private String dishName;
    private BigDecimal price;
    private int quantity;
    private BigDecimal amount;
    private boolean refunded;
    private int refundedQuantity;
    private List<RecipeItem> recipeSnapshot;

    public OrderItem() {
        this.quantity = 1;
        this.refunded = false;
        this.refundedQuantity = 0;
        this.recipeSnapshot = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDishId() {
        return dishId;
    }

    public void setDishId(String dishId) {
        this.dishId = dishId;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public boolean isRefunded() {
        return refunded;
    }

    public void setRefunded(boolean refunded) {
        this.refunded = refunded;
    }

    public int getRefundedQuantity() {
        return refundedQuantity;
    }

    public void setRefundedQuantity(int refundedQuantity) {
        this.refundedQuantity = refundedQuantity;
    }

    public List<RecipeItem> getRecipeSnapshot() {
        return recipeSnapshot;
    }

    public void setRecipeSnapshot(List<RecipeItem> recipeSnapshot) {
        this.recipeSnapshot = recipeSnapshot;
    }
}