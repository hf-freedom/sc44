package com.restaurant.model;

import java.math.BigDecimal;

public class RecipeItem {
    private String ingredientId;
    private String ingredientName;
    private BigDecimal quantity;

    public RecipeItem() {
    }

    public RecipeItem(String ingredientId, String ingredientName, BigDecimal quantity) {
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
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
}