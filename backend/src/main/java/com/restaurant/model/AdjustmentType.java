package com.restaurant.model;

public enum AdjustmentType {
    REFUND_AFTER_SETTLEMENT("日结后退款"),
    OTHER("其他调整");

    private final String description;

    AdjustmentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}