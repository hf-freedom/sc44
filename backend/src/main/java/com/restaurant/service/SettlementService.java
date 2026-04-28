package com.restaurant.service;

import com.restaurant.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class SettlementService {

    @Autowired
    private DataStorageService dataStorageService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private WasteService wasteService;

    @Autowired
    private DishService dishService;

    public List<DailySettlement> getAllSettlements() {
        List<DailySettlement> settlements = new ArrayList<>(dataStorageService.getSettlements().values());
        settlements.sort((s1, s2) -> {
            if (s1.getSettlementDate() == null) return 1;
            if (s2.getSettlementDate() == null) return -1;
            return s2.getSettlementDate().compareTo(s1.getSettlementDate());
        });
        return settlements;
    }

    public DailySettlement getSettlementById(String id) {
        return dataStorageService.getSettlements().get(id);
    }

    public DailySettlement getTodaySettlement() {
        LocalDate today = LocalDate.now();
        for (DailySettlement settlement : dataStorageService.getSettlements().values()) {
            if (today.equals(settlement.getSettlementDate())) {
                return settlement;
            }
        }
        return null;
    }

    public DailySettlement createTodaySettlement() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        DailySettlement settlement = new DailySettlement();
        settlement.setId(UUID.randomUUID().toString());
        settlement.setSettlementDate(today);
        settlement.setCreateTime(LocalDateTime.now());

        int totalOrders = 0;
        int cancelledOrders = 0;
        BigDecimal totalSales = BigDecimal.ZERO;
        BigDecimal totalRefund = BigDecimal.ZERO;
        BigDecimal totalIngredientCost = BigDecimal.ZERO;

        for (Order order : dataStorageService.getOrders().values()) {
            if (order.getCreateTime() == null) continue;
            if (order.getCreateTime().isBefore(startOfDay) || order.getCreateTime().isAfter(endOfDay)) {
                continue;
            }

            totalOrders++;
            if (order.getStatus() == OrderStatus.CANCELLED) {
                cancelledOrders++;
            }
            if (order.getPaidAmount() != null) {
                totalSales = totalSales.add(order.getPaidAmount());
            }
            if (order.getRefundAmount() != null) {
                totalRefund = totalRefund.add(order.getRefundAmount());
            }

            if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.REFUNDED) {
                for (OrderItem item : order.getItems()) {
                    int effectiveQuantity = item.getQuantity() - item.getRefundedQuantity();
                    if (effectiveQuantity > 0 && item.getRecipeSnapshot() != null) {
                        for (RecipeItem recipeItem : item.getRecipeSnapshot()) {
                            Ingredient ingredient = getIngredientById(recipeItem.getIngredientId());
                            if (ingredient != null && ingredient.getUnitPrice() != null) {
                                BigDecimal cost = ingredient.getUnitPrice()
                                        .multiply(recipeItem.getQuantity())
                                        .multiply(new BigDecimal(effectiveQuantity));
                                totalIngredientCost = totalIngredientCost.add(cost);
                            }
                        }
                    }
                }
            }
        }

        BigDecimal totalWasteAmount = wasteService.getTodayWasteAmount();
        BigDecimal netSales = totalSales.subtract(totalRefund);
        BigDecimal grossProfit = netSales.subtract(totalIngredientCost).subtract(totalWasteAmount);
        BigDecimal grossProfitRate = BigDecimal.ZERO;
        if (netSales.compareTo(BigDecimal.ZERO) > 0) {
            grossProfitRate = grossProfit.divide(netSales, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        }

        settlement.setTotalOrders(totalOrders);
        settlement.setCancelledOrders(cancelledOrders);
        settlement.setTotalSales(totalSales);
        settlement.setTotalRefund(totalRefund);
        settlement.setNetSales(netSales);
        settlement.setTotalIngredientCost(totalIngredientCost);
        settlement.setTotalWasteAmount(totalWasteAmount);
        settlement.setGrossProfit(grossProfit);
        settlement.setGrossProfitRate(grossProfitRate);

        dataStorageService.getSettlements().put(settlement.getId(), settlement);
        return settlement;
    }

    public DailySettlement confirmSettlement(String settlementId) {
        DailySettlement settlement = getSettlementById(settlementId);
        if (settlement == null) {
            throw new RuntimeException("日结单不存在");
        }
        if (settlement.isConfirmed()) {
            throw new RuntimeException("日结单已确认");
        }

        settlement.setConfirmed(true);
        settlement.setConfirmTime(LocalDateTime.now());

        markOrdersAsSettled(settlement.getSettlementDate());

        return settlement;
    }

    private void markOrdersAsSettled(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        for (Order order : dataStorageService.getOrders().values()) {
            if (order.getCreateTime() == null) continue;
            if (order.getCreateTime().isBefore(startOfDay) || order.getCreateTime().isAfter(endOfDay)) {
                continue;
            }
            order.setSettled(true);
        }
    }

    public SettlementAdjustment createRefundAdjustment(Order order, String reason) {
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        LocalDate settlementDate = order.getCreateTime().toLocalDate();
        DailySettlement settlement = null;
        for (DailySettlement s : dataStorageService.getSettlements().values()) {
            if (settlementDate.equals(s.getSettlementDate())) {
                settlement = s;
                break;
            }
        }

        SettlementAdjustment adjustment = new SettlementAdjustment();
        adjustment.setId(UUID.randomUUID().toString());
        adjustment.setRelatedOrderId(order.getId());
        adjustment.setRelatedOrderNo(order.getOrderNo());
        adjustment.setType(AdjustmentType.REFUND_AFTER_SETTLEMENT);
        adjustment.setAmount(order.getTotalAmount());
        adjustment.setReason(reason);
        adjustment.setOriginalSettlementDate(settlementDate);
        if (settlement != null) {
            adjustment.setOriginalSettlementId(settlement.getId());
        }

        dataStorageService.getAdjustments().put(adjustment.getId(), adjustment);
        return adjustment;
    }

    public List<SettlementAdjustment> getAllAdjustments() {
        List<SettlementAdjustment> adjustments = new ArrayList<>(dataStorageService.getAdjustments().values());
        adjustments.sort((a1, a2) -> {
            if (a1.getCreateTime() == null) return 1;
            if (a2.getCreateTime() == null) return -1;
            return a2.getCreateTime().compareTo(a1.getCreateTime());
        });
        return adjustments;
    }

    private Ingredient getIngredientById(String id) {
        return dataStorageService.getIngredients().get(id);
    }
}