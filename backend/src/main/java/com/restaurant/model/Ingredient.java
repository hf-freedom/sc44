package com.restaurant.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Ingredient {
    private String id;
    private String name;
    private String unit;
    private BigDecimal inventory;
    private BigDecimal lockedInventory;
    private BigDecimal safeInventory;
    private LocalDate expiryDate;
    private BigDecimal unitPrice;
    private LocalDate lastUpdateTime;

    public Ingredient() {
        this.lockedInventory = BigDecimal.ZERO;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getInventory() {
        return inventory;
    }

    public void setInventory(BigDecimal inventory) {
        this.inventory = inventory;
    }

    public BigDecimal getLockedInventory() {
        return lockedInventory;
    }

    public void setLockedInventory(BigDecimal lockedInventory) {
        this.lockedInventory = lockedInventory;
    }

    public BigDecimal getSafeInventory() {
        return safeInventory;
    }

    public void setSafeInventory(BigDecimal safeInventory) {
        this.safeInventory = safeInventory;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public LocalDate getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(LocalDate lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public BigDecimal getAvailableInventory() {
        if (inventory == null) return BigDecimal.ZERO;
        if (lockedInventory == null) return inventory;
        return inventory.subtract(lockedInventory);
    }

    public boolean isLowInventory() {
        if (safeInventory == null || inventory == null) return false;
        return inventory.compareTo(safeInventory) < 0;
    }

    public boolean isExpired() {
        if (expiryDate == null) return false;
        return LocalDate.now().isAfter(expiryDate);
    }
}