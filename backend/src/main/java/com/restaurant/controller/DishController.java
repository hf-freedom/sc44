package com.restaurant.controller;

import com.restaurant.model.ApiResponse;
import com.restaurant.model.Dish;
import com.restaurant.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/dishes")
public class DishController {

    @Autowired
    private DishService dishService;

    @GetMapping
    public ApiResponse<List<Dish>> getAllDishes() {
        return ApiResponse.success(dishService.getAllDishes());
    }

    @GetMapping("/{id}")
    public ApiResponse<Dish> getDishById(@PathVariable String id) {
        Dish dish = dishService.getDishById(id);
        if (dish == null) {
            return ApiResponse.error("菜品不存在");
        }
        return ApiResponse.success(dish);
    }

    @PostMapping
    public ApiResponse<Dish> createDish(@RequestBody Dish dish) {
        try {
            Dish created = dishService.createDish(dish);
            return ApiResponse.success("创建成功", created);
        } catch (Exception e) {
            return ApiResponse.error("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<Dish> updateDish(@PathVariable String id, @RequestBody Dish dish) {
        try {
            Dish updated = dishService.updateDish(id, dish);
            if (updated == null) {
                return ApiResponse.error("菜品不存在");
            }
            return ApiResponse.success("更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDish(@PathVariable String id) {
        boolean deleted = dishService.deleteDish(id);
        if (!deleted) {
            return ApiResponse.error("菜品不存在");
        }
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping("/{id}/cost")
    public ApiResponse<BigDecimal> calculateDishCost(@PathVariable String id) {
        Dish dish = dishService.getDishById(id);
        if (dish == null) {
            return ApiResponse.error("菜品不存在");
        }
        return ApiResponse.success(dishService.calculateDishCost(dish));
    }

    @GetMapping("/{id}/can-make")
    public ApiResponse<Boolean> canMakeDish(@PathVariable String id, @RequestParam(defaultValue = "1") int quantity) {
        boolean canMake = dishService.canMakeDish(id, quantity);
        return ApiResponse.success(canMake);
    }

    @PostMapping("/update-availability")
    public ApiResponse<Void> updateDishAvailability() {
        dishService.updateDishAvailability();
        return ApiResponse.success("菜品可用性已更新", null);
    }
}