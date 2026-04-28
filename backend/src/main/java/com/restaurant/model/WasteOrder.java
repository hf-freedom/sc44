package com.restaurant.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WasteOrder {
    private String id;
    private String orderNo;
    private LocalDateTime createTime;
    private WasteReason reason;
    private List<WasteOrderItem> items;
    private BigDecimal totalAmount;
    private String remark;

    public WasteOrder() {
        this.items = new ArrayList<>();
        this.totalAmount = BigDecimal.ZERO;
        this.createTime = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public WasteReason getReason() {
        return reason;
    }

    public void setReason(WasteReason reason) {
        this.reason = reason;
    }

    public List<WasteOrderItem> getItems() {
        return items;
    }

    public void setItems(List<WasteOrderItem> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}