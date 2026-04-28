package com.restaurant.service;

import com.restaurant.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class DishService {

    @Autowired
    private DataStorageService dataStorageService;

    @Autowired
    private IngredientService ingredientService;

    public List<Dish> getAllDishes() {
        return new ArrayList<>(dataStorageService.getDishes().values());
    }

    public Dish getDishById(String id) {
        return dataStorageService.getDishes().get(id);
    }

    public Dish createDish(Dish dish) {
        String id = UUID.randomUUID().toString();
        dish.setId(id);
        if (dish.getStatus() == null) {
            dish.setStatus(DishStatus.AVAILABLE);
        }
        if (dish.getRecipe() == null) {
            dish.setRecipe(new ArrayList<>());
        }
        dataStorageService.getDishes().put(id, dish);
        updateDishAvailability(dish);
        return dish;
    }

    public Dish updateDish(String id, Dish dish) {
        Dish existing = dataStorageService.getDishes().get(id);
        if (existing == null) {
            return null;
        }
        existing.setName(dish.getName());
        if (dish.getPrice() != null) {
            existing.setPrice(dish.getPrice());
        }
        if (dish.getRecipe() != null) {
            existing.setRecipe(dish.getRecipe());
        }
        updateDishAvailability(existing);
        return existing;
    }

    public boolean deleteDish(String id) {
        return dataStorageService.getDishes().remove(id) != null;
    }

    public boolean canMakeDish(Dish dish, int quantity) {
        if (dish == null || dish.getRecipe() == null) {
            return false;
        }
        for (RecipeItem item : dish.getRecipe()) {
            BigDecimal required = item.getQuantity().multiply(new BigDecimal(quantity));
            if (!ingredientService.hasEnoughInventory(item.getIngredientId(), required)) {
                return false;
            }
        }
        return true;
    }

    public boolean canMakeDish(String dishId, int quantity) {
        Dish dish = getDishById(dishId);
        return canMakeDish(dish, quantity);
    }

    public void updateDishAvailability() {
        for (Dish dish : dataStorageService.getDishes().values()) {
            updateDishAvailability(dish);
        }
    }

    public void updateDishAvailability(Dish dish) {
        if (dish == null) return;
        
        boolean canMake = canMakeDish(dish, 1);
        if (canMake) {
            if (dish.getStatus() == DishStatus.UNAVAILABLE || dish.getStatus() == DishStatus.SOLD_OUT) {
                dish.setStatus(DishStatus.AVAILABLE);
            }
        } else {
            if (dish.getStatus() == DishStatus.AVAILABLE) {
                dish.setStatus(DishStatus.SOLD_OUT);
            }
        }
    }

    public BigDecimal calculateDishCost(Dish dish) {
        if (dish == null || dish.getRecipe() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal cost = BigDecimal.ZERO;
        for (RecipeItem item : dish.getRecipe()) {
            Ingredient ingredient = ingredientService.getIngredientById(item.getIngredientId());
            if (ingredient != null && ingredient.getUnitPrice() != null) {
                cost = cost.add(ingredient.getUnitPrice().multiply(item.getQuantity()));
            }
        }
        return cost;
    }

    public void lockIngredientsForDish(Dish dish, int quantity) {
        if (dish == null || dish.getRecipe() == null) {
            return;
        }
        for (RecipeItem item : dish.getRecipe()) {
            BigDecimal lockAmount = item.getQuantity().multiply(new BigDecimal(quantity));
            ingredientService.lockInventory(item.getIngredientId(), lockAmount);
        }
    }

    public void unlockIngredientsForDish(Dish dish, int quantity) {
        if (dish == null || dish.getRecipe() == null) {
            return;
        }
        for (RecipeItem item : dish.getRecipe()) {
            BigDecimal unlockAmount = item.getQuantity().multiply(new BigDecimal(quantity));
            ingredientService.unlockInventory(item.getIngredientId(), unlockAmount);
        }
    }

    public void consumeIngredientsForDish(Dish dish, int quantity) {
        if (dish == null || dish.getRecipe() == null) {
            return;
        }
        for (RecipeItem item : dish.getRecipe()) {
            BigDecimal consumeAmount = item.getQuantity().multiply(new BigDecimal(quantity));
            ingredientService.consumeInventory(item.getIngredientId(), consumeAmount);
        }
    }
}