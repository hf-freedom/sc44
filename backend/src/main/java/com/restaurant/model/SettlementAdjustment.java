package com.restaurant.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SettlementAdjustment {
    private String id;
    private String relatedOrderId;
    private String relatedOrderNo;
    private LocalDate originalSettlementDate;
    private String originalSettlementId;
    private LocalDateTime createTime;
    private AdjustmentType type;
    private BigDecimal amount;
    private String reason;
    private String remark;

    public SettlementAdjustment() {
        this.createTime = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRelatedOrderId() {
        return relatedOrderId;
    }

    public void setRelatedOrderId(String relatedOrderId) {
        this.relatedOrderId = relatedOrderId;
    }

    public String getRelatedOrderNo() {
        return relatedOrderNo;
    }

    public void setRelatedOrderNo(String relatedOrderNo) {
        this.relatedOrderNo = relatedOrderNo;
    }

    public LocalDate getOriginalSettlementDate() {
        return originalSettlementDate;
    }

    public void setOriginalSettlementDate(LocalDate originalSettlementDate) {
        this.originalSettlementDate = originalSettlementDate;
    }

    public String getOriginalSettlementId() {
        return originalSettlementId;
    }

    public void setOriginalSettlementId(String originalSettlementId) {
        this.originalSettlementId = originalSettlementId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public AdjustmentType getType() {
        return type;
    }

    public void setType(AdjustmentType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}