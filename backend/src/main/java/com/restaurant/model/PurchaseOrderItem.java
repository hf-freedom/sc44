package com.restaurant.model;

import java.math.BigDecimal;

public class PurchaseOrderItem {
    private String id;
    private String ingredientId;
    private String ingredientName;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private String unit;
    private BigDecimal safeInventory;
    private BigDecimal currentInventory;

    public PurchaseOrderItem() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(String ingredientId) {
        this.ingredientId = ingredientId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getSafeInventory() {
        return safeInventory;
    }

    public void setSafeInventory(BigDecimal safeInventory) {
        this.safeInventory = safeInventory;
    }

    public BigDecimal getCurrentInventory() {
        return currentInventory;
    }

    public void setCurrentInventory(BigDecimal currentInventory) {
        this.currentInventory = currentInventory;
    }
}