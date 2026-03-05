package com.example.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;


@Entity
@Table(name = "gpf_advance_rules")
public class GpfAdvanceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Long ruleId;

    @Column(name = "rule_code", nullable = false, unique = true, length = 20)
    private String ruleCode;

    @Column(name = "advance_reason", nullable = false, length = 1000)
    private String advanceReason;

    @Column(name = "rule_description", length = 1000)
    private String ruleDescription;

    @Column(name = "max_percentage", precision = 5, scale = 2)
    private BigDecimal maxPercentage;

    @Column(name = "max_months_pay")
    private Integer maxMonthsPay;

   @Column(name = "is_special_case")
private Boolean specialCase;
    @Column(name = "min_service_years")
    private Integer minServiceYears;

    @Column(name = "is_active")
private Boolean active = true;

    @Column(name = "createdat")
    private LocalDateTime createdat;

    @Column(name = "createdby", length = 50)
    private String createdby;

    @PrePersist
public void prePersist() {
    createdat = LocalDateTime.now();
    if (active == null) active = true;
}
    /* ================= GETTERS ================= */

    public Long getRuleId() {
        return ruleId;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public String getAdvanceReason() {
        return advanceReason;
    }

    public String getRuleDescription() {
        return ruleDescription;
    }

    public BigDecimal getMaxPercentage() {
        return maxPercentage;
    }

    public Integer getMaxMonthsPay() {
        return maxMonthsPay;
    }

 public Boolean getSpecialCase() {
    return specialCase;
}

    public Integer getMinServiceYears() {
        return minServiceYears;
    }

 public Boolean getIsActive() {
    return active;
}

    public LocalDateTime getCreatedat() {
        return createdat;
    }

    public String getCreatedby() {
        return createdby;
    }

    /* ================= SETTERS ================= */

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public void setAdvanceReason(String advanceReason) {
        this.advanceReason = advanceReason;
    }

    public void setRuleDescription(String ruleDescription) {
        this.ruleDescription = ruleDescription;
    }

    public void setMaxPercentage(BigDecimal maxPercentage) {
        this.maxPercentage = maxPercentage;
    }

    public void setMaxMonthsPay(Integer maxMonthsPay) {
        this.maxMonthsPay = maxMonthsPay;
    }

 public void setSpecialCase(Boolean specialCase) {
    this.specialCase = specialCase;
}

    public void setMinServiceYears(Integer minServiceYears) {
        this.minServiceYears = minServiceYears;
    }

 public void setIsActive(Boolean isActive) {
    this.active = isActive;
}

    public void setCreatedat(LocalDateTime createdat) {
        this.createdat = createdat;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }
}