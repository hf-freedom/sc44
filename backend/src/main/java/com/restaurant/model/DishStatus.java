package com.restaurant.model;

public enum DishStatus {
    AVAILABLE("可售"),
    UNAVAILABLE("不可售"),
    SOLD_OUT("售罄");

    private final String description;

    DishStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}