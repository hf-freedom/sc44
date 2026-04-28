package com.restaurant.model;

public enum WasteReason {
    EXPIRED("过期"),
    DAMAGED("损坏"),
    MISTAKE("操作失误"),
    OTHER("其他");

    private final String description;

    WasteReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}