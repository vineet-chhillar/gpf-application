package com.example.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gpf_withdrawl_rules")
public class GpfWithdrawlRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Long ruleId;

    @Column(name = "rule_code", nullable = false, unique = true)
    private String ruleCode;

    @Column(name = "rule_description", nullable = false)
    private String ruleDescription;

    @Column(name = "max_percentage")
    private BigDecimal maxPercentage;

    @Column(name = "max_months_pay")
    private Integer maxMonthsPay;

    @Column(name = "requires_vehicle_cost")
    private Boolean requiresVehicleCost;

    @Column(name = "min_service_years")
    private Integer minServiceYears;

    @Column(name = "retirement_window_yrs")
    private Integer retirementWindowYrs;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "createdat", nullable = false, updatable = false)
    private LocalDateTime createdat;

    @Column(name = "createdby", nullable = false)
    private String createdby;



    @PrePersist
    public void onCreate() {
        this.createdat = LocalDateTime.now();
        if (this.createdby == null) {
            this.createdby = "SYSTEM";
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
        
    }

    /* ===== Getters & Setters ===== */

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getRuleDescription() {
        return ruleDescription;
    }

    public void setRuleDescription(String ruleDescription) {
        this.ruleDescription = ruleDescription;
    }

    public BigDecimal getMaxPercentage() {
        return maxPercentage;
    }

    public void setMaxPercentage(BigDecimal maxPercentage) {
        this.maxPercentage = maxPercentage;
    }

    public Integer getMaxMonthsPay() {
        return maxMonthsPay;
    }

    public void setMaxMonthsPay(Integer maxMonthsPay) {
        this.maxMonthsPay = maxMonthsPay;
    }

    public Boolean getRequiresVehicleCost() {
        return requiresVehicleCost;
    }

    public void setRequiresVehicleCost(Boolean requiresVehicleCost) {
        this.requiresVehicleCost = requiresVehicleCost;
    }

    public Integer getMinServiceYears() {
        return minServiceYears;
    }

    public void setMinServiceYears(Integer minServiceYears) {
        this.minServiceYears = minServiceYears;
    }

    public Integer getRetirementWindowYrs() {
        return retirementWindowYrs;
    }

    public void setRetirementWindowYrs(Integer retirementWindowYrs) {
        this.retirementWindowYrs = retirementWindowYrs;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedat() {
        return createdat;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    
}
