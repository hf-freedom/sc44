package com.restaurant.controller;

import com.restaurant.model.ApiResponse;
import com.restaurant.model.DailySettlement;
import com.restaurant.model.SettlementAdjustment;
import com.restaurant.service.SettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settlements")
public class SettlementController {

    @Autowired
    private SettlementService settlementService;

    @GetMapping
    public ApiResponse<List<DailySettlement>> getAllSettlements() {
        return ApiResponse.success(settlementService.getAllSettlements());
    }

    @GetMapping("/today")
    public ApiResponse<DailySettlement> getTodaySettlement() {
        DailySettlement settlement = settlementService.getTodaySettlement();
        if (settlement == null) {
            return ApiResponse.success("今日日结单未创建", null);
        }
        return ApiResponse.success(settlement);
    }

    @GetMapping("/{id}")
    public ApiResponse<DailySettlement> getSettlementById(@PathVariable String id) {
        DailySettlement settlement = settlementService.getSettlementById(id);
        if (settlement == null) {
            return ApiResponse.error("日结单不存在");
        }
        return ApiResponse.success(settlement);
    }

    @PostMapping("/create-today")
    public ApiResponse<DailySettlement> createTodaySettlement() {
        try {
            DailySettlement settlement = settlementService.createTodaySettlement();
            return ApiResponse.success("日结单创建成功", settlement);
        } catch (Exception e) {
            return ApiResponse.error("日结单创建失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<DailySettlement> confirmSettlement(@PathVariable String id) {
        try {
            DailySettlement settlement = settlementService.confirmSettlement(id);
            return ApiResponse.success("日结单确认成功", settlement);
        } catch (Exception e) {
            return ApiResponse.error("日结单确认失败: " + e.getMessage());
        }
    }

    @GetMapping("/adjustments")
    public ApiResponse<List<SettlementAdjustment>> getAllAdjustments() {
        return ApiResponse.success(settlementService.getAllAdjustments());
    }
}