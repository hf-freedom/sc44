package com.restaurant.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DailySettlement {
    private String id;
    private LocalDate settlementDate;
    private LocalDateTime createTime;
    private LocalDateTime confirmTime;
    private boolean confirmed;

    private int totalOrders;
    private int cancelledOrders;
    private BigDecimal totalSales;
    private BigDecimal totalRefund;
    private BigDecimal netSales;

    private BigDecimal totalIngredientCost;
    private BigDecimal totalWasteAmount;

    private BigDecimal grossProfit;
    private BigDecimal grossProfitRate;

    private String remark;

    public DailySettlement() {
        this.confirmed = false;
        this.totalOrders = 0;
        this.cancelledOrders = 0;
        this.totalSales = BigDecimal.ZERO;
        this.totalRefund = BigDecimal.ZERO;
        this.netSales = BigDecimal.ZERO;
        this.totalIngredientCost = BigDecimal.ZERO;
        this.totalWasteAmount = BigDecimal.ZERO;
        this.grossProfit = BigDecimal.ZERO;
        this.grossProfitRate = BigDecimal.ZERO;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(LocalDate settlementDate) {
        this.settlementDate = settlementDate;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getConfirmTime() {
        return confirmTime;
    }

    public void setConfirmTime(LocalDateTime confirmTime) {
        this.confirmTime = confirmTime;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public int getCancelledOrders() {
        return cancelledOrders;
    }

    public void setCancelledOrders(int cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public BigDecimal getTotalRefund() {
        return totalRefund;
    }

    public void setTotalRefund(BigDecimal totalRefund) {
        this.totalRefund = totalRefund;
    }

    public BigDecimal getNetSales() {
        return netSales;
    }

    public void setNetSales(BigDecimal netSales) {
        this.netSales = netSales;
    }

    public BigDecimal getTotalIngredientCost() {
        return totalIngredientCost;
    }

    public void setTotalIngredientCost(BigDecimal totalIngredientCost) {
        this.totalIngredientCost = totalIngredientCost;
    }

    public BigDecimal getTotalWasteAmount() {
        return totalWasteAmount;
    }

    public void setTotalWasteAmount(BigDecimal totalWasteAmount) {
        this.totalWasteAmount = totalWasteAmount;
    }

    public BigDecimal getGrossProfit() {
        return grossProfit;
    }

    public void setGrossProfit(BigDecimal grossProfit) {
        this.grossProfit = grossProfit;
    }

    public BigDecimal getGrossProfitRate() {
        return grossProfitRate;
    }

    public void setGrossProfitRate(BigDecimal grossProfitRate) {
        this.grossProfitRate = grossProfitRate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}