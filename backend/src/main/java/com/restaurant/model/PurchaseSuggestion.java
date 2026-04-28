package com.restaurant.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PurchaseSuggestion {
    private String id;
    private String ingredientId;
    private String ingredientName;
    private BigDecimal currentInventory;
    private BigDecimal safeInventory;
    private BigDecimal suggestedQuantity;
    private String unit;
    private LocalDateTime createTime;
    private boolean converted;
    private String convertedPurchaseOrderId;
    private LocalDateTime convertTime;

    public PurchaseSuggestion() {
        this.converted = false;
        this.createTime = LocalDateTime.now();
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

    public BigDecimal getCurrentInventory() {
        return currentInventory;
    }

    public void setCurrentInventory(BigDecimal currentInventory) {
        this.currentInventory = currentInventory;
    }

    public BigDecimal getSafeInventory() {
        return safeInventory;
    }

    public void setSafeInventory(BigDecimal safeInventory) {
        this.safeInventory = safeInventory;
    }

    public BigDecimal getSuggestedQuantity() {
        return suggestedQuantity;
    }

    public void setSuggestedQuantity(BigDecimal suggestedQuantity) {
        this.suggestedQuantity = suggestedQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public boolean isConverted() {
        return converted;
    }

    public void setConverted(boolean converted) {
        this.converted = converted;
    }

    public String getConvertedPurchaseOrderId() {
        return convertedPurchaseOrderId;
    }

    public void setConvertedPurchaseOrderId(String convertedPurchaseOrderId) {
        this.convertedPurchaseOrderId = convertedPurchaseOrderId;
    }

    public LocalDateTime getConvertTime() {
        return convertTime;
    }

    public void setConvertTime(LocalDateTime convertTime) {
        this.convertTime = convertTime;
    }
}