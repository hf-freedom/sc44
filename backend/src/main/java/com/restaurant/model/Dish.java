package com.restaurant.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Dish {
    private String id;
    private String name;
    private BigDecimal price;
    private DishStatus status;
    private List<RecipeItem> recipe;

    public Dish() {
        this.recipe = new ArrayList<>();
        this.status = DishStatus.AVAILABLE;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public DishStatus getStatus() {
        return status;
    }

    public void setStatus(DishStatus status) {
        this.status = status;
    }

    public List<RecipeItem> getRecipe() {
        return recipe;
    }

    public void setRecipe(List<RecipeItem> recipe) {
        this.recipe = recipe;
    }
}