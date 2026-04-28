package com.restaurant.model;

public enum PurchaseOrderStatus {
    PENDING("待到货"),
    RECEIVED("已到货");

    private final String description;

    PurchaseOrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}