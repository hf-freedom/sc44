package com.restaurant.controller;

import com.restaurant.model.ApiResponse;
import com.restaurant.model.Ingredient;
import com.restaurant.service.IngredientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
public class IngredientController {

    @Autowired
    private IngredientService ingredientService;

    @GetMapping
    public ApiResponse<List<Ingredient>> getAllIngredients() {
        return ApiResponse.success(ingredientService.getAllIngredients());
    }

    @GetMapping("/{id}")
    public ApiResponse<Ingredient> getIngredientById(@PathVariable String id) {
        Ingredient ingredient = ingredientService.getIngredientById(id);
        if (ingredient == null) {
            return ApiResponse.error("原材料不存在");
        }
        return ApiResponse.success(ingredient);
    }

    @PostMapping
    public ApiResponse<Ingredient> createIngredient(@RequestBody Ingredient ingredient) {
        try {
            Ingredient created = ingredientService.createIngredient(ingredient);
            return ApiResponse.success("创建成功", created);
        } catch (Exception e) {
            return ApiResponse.error("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<Ingredient> updateIngredient(@PathVariable String id, @RequestBody Ingredient ingredient) {
        try {
            Ingredient updated = ingredientService.updateIngredient(id, ingredient);
            if (updated == null) {
                return ApiResponse.error("原材料不存在");
            }
            return ApiResponse.success("更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteIngredient(@PathVariable String id) {
        boolean deleted = ingredientService.deleteIngredient(id);
        if (!deleted) {
            return ApiResponse.error("原材料不存在");
        }
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping("/low-inventory")
    public ApiResponse<List<Ingredient>> getLowInventoryIngredients() {
        return ApiResponse.success(ingredientService.getLowInventoryIngredients());
    }

    @GetMapping("/expired")
    public ApiResponse<List<Ingredient>> getExpiredIngredients() {
        return ApiResponse.success(ingredientService.getExpiredIngredients());
    }
}