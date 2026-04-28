package com.restaurant.service;

import com.restaurant.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class IngredientService {

    @Autowired
    private DataStorageService dataStorageService;

    @Autowired
    private DishService dishService;

    public List<Ingredient> getAllIngredients() {
        return new ArrayList<>(dataStorageService.getIngredients().values());
    }

    public Ingredient getIngredientById(String id) {
        return dataStorageService.getIngredients().get(id);
    }

    public Ingredient createIngredient(Ingredient ingredient) {
        String id = UUID.randomUUID().toString();
        ingredient.setId(id);
        if (ingredient.getLockedInventory() == null) {
            ingredient.setLockedInventory(BigDecimal.ZERO);
        }
        if (ingredient.getLastUpdateTime() == null) {
            ingredient.setLastUpdateTime(LocalDate.now());
        }
        dataStorageService.getIngredients().put(id, ingredient);
        return ingredient;
    }

    public Ingredient updateIngredient(String id, Ingredient ingredient) {
        Ingredient existing = dataStorageService.getIngredients().get(id);
        if (existing == null) {
            return null;
        }
        existing.setName(ingredient.getName());
        existing.setUnit(ingredient.getUnit());
        if (ingredient.getInventory() != null) {
            existing.setInventory(ingredient.getInventory());
        }
        if (ingredient.getSafeInventory() != null) {
            existing.setSafeInventory(ingredient.getSafeInventory());
        }
        if (ingredient.getExpiryDate() != null) {
            existing.setExpiryDate(ingredient.getExpiryDate());
        }
        if (ingredient.getUnitPrice() != null) {
            existing.setUnitPrice(ingredient.getUnitPrice());
        }
        existing.setLastUpdateTime(LocalDate.now());
        
        dishService.updateDishAvailability();
        
        return existing;
    }

    public boolean deleteIngredient(String id) {
        return dataStorageService.getIngredients().remove(id) != null;
    }

    public void updateInventory(String ingredientId, BigDecimal quantity) {
        Ingredient ingredient = dataStorageService.getIngredients().get(ingredientId);
        if (ingredient != null) {
            if (ingredient.getInventory() == null) {
                ingredient.setInventory(BigDecimal.ZERO);
            }
            ingredient.setInventory(ingredient.getInventory().add(quantity));
            ingredient.setLastUpdateTime(LocalDate.now());
            dishService.updateDishAvailability();
        }
    }

    public void lockInventory(String ingredientId, BigDecimal quantity) {
        Ingredient ingredient = dataStorageService.getIngredients().get(ingredientId);
        if (ingredient != null) {
            if (ingredient.getLockedInventory() == null) {
                ingredient.setLockedInventory(BigDecimal.ZERO);
            }
            ingredient.setLockedInventory(ingredient.getLockedInventory().add(quantity));
        }
    }

    public void unlockInventory(String ingredientId, BigDecimal quantity) {
        Ingredient ingredient = dataStorageService.getIngredients().get(ingredientId);
        if (ingredient != null && ingredient.getLockedInventory() != null) {
            ingredient.setLockedInventory(ingredient.getLockedInventory().subtract(quantity));
            if (ingredient.getLockedInventory().compareTo(BigDecimal.ZERO) < 0) {
                ingredient.setLockedInventory(BigDecimal.ZERO);
            }
        }
    }

    public void consumeInventory(String ingredientId, BigDecimal quantity) {
        Ingredient ingredient = dataStorageService.getIngredients().get(ingredientId);
        if (ingredient != null) {
            if (ingredient.getInventory() != null) {
                ingredient.setInventory(ingredient.getInventory().subtract(quantity));
            }
            if (ingredient.getLockedInventory() != null) {
                ingredient.setLockedInventory(ingredient.getLockedInventory().subtract(quantity));
                if (ingredient.getLockedInventory().compareTo(BigDecimal.ZERO) < 0) {
                    ingredient.setLockedInventory(BigDecimal.ZERO);
                }
            }
            ingredient.setLastUpdateTime(LocalDate.now());
            dishService.updateDishAvailability();
        }
    }

    public boolean hasEnoughInventory(String ingredientId, BigDecimal requiredQuantity) {
        Ingredient ingredient = dataStorageService.getIngredients().get(ingredientId);
        if (ingredient == null) {
            return false;
        }
        BigDecimal available = ingredient.getAvailableInventory();
        return available.compareTo(requiredQuantity) >= 0;
    }

    public List<Ingredient> getLowInventoryIngredients() {
        List<Ingredient> result = new ArrayList<>();
        for (Ingredient ingredient : dataStorageService.getIngredients().values()) {
            if (ingredient.isLowInventory()) {
                result.add(ingredient);
            }
        }
        return result;
    }

    public List<Ingredient> getExpiredIngredients() {
        List<Ingredient> result = new ArrayList<>();
        for (Ingredient ingredient : dataStorageService.getIngredients().values()) {
            if (ingredient.isExpired() && ingredient.getInventory() != null 
                    && ingredient.getInventory().compareTo(BigDecimal.ZERO) > 0) {
                result.add(ingredient);
            }
        }
        return result;
    }
}