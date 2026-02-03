package com.example.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ApplicationDetailsDTO {

    private Long applicationId;

    // Employee
    private String empCode;
    private String empName;
    private String designation;
    private String division;

    // Withdrawal
    private BigDecimal amountRequested;
    private String purposeOfWithdrawal;
    private String withdrawalRule;
    private LocalDate dateOfApplication;
// Credit period
private LocalDate creditFromDate;
private LocalDate creditToDate;
private BigDecimal totalCreditAmount;
private BigDecimal refundAfterOutstandingBalance;

// Withdrawal period
private LocalDate withdrawlFromDate;
private LocalDate withdrawlToDate;
private BigDecimal totalWithdrawlAmount;

    // Financial
    private BigDecimal basicPay;
    private BigDecimal outstandingBalance;
    private BigDecimal netBalance;

    // Workflow
    private String statusCode;
    private String currentOwnerRole;

    // ===== GETTERS & SETTERS =====

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getEmpCode() {
        return empCode;
    }

    public void setEmpCode(String empCode) {
        this.empCode = empCode;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public BigDecimal getAmountRequested() {
        return amountRequested;
    }

    public void setAmountRequested(BigDecimal amountRequested) {
        this.amountRequested = amountRequested;
    }

    public String getPurposeOfWithdrawal() {
        return purposeOfWithdrawal;
    }

    public void setPurposeOfWithdrawal(String purposeOfWithdrawal) {
        this.purposeOfWithdrawal = purposeOfWithdrawal;
    }

    public String getWithdrawalRule() {
        return withdrawalRule;
    }

    public void setWithdrawalRule(String withdrawalRule) {
        this.withdrawalRule = withdrawalRule;
    }

    public LocalDate getDateOfApplication() {
        return dateOfApplication;
    }

    public void setDateOfApplication(LocalDate dateOfApplication) {
        this.dateOfApplication = dateOfApplication;
    }

    public BigDecimal getBasicPay() {
        return basicPay;
    }

    public void setBasicPay(BigDecimal basicPay) {
        this.basicPay = basicPay;
    }

    public BigDecimal getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(BigDecimal outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public BigDecimal getNetBalance() {
        return netBalance;
    }

    public void setNetBalance(BigDecimal netBalance) {
        this.netBalance = netBalance;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getCurrentOwnerRole() {
        return currentOwnerRole;
    }

    public void setCurrentOwnerRole(String currentOwnerRole) {
        this.currentOwnerRole = currentOwnerRole;
    }
    public LocalDate getCreditFromDate() {
    return creditFromDate;
}
public void setCreditFromDate(LocalDate creditFromDate) {
    this.creditFromDate = creditFromDate;
}

public LocalDate getCreditToDate() {
    return creditToDate;
}
public void setCreditToDate(LocalDate creditToDate) {
    this.creditToDate = creditToDate;
}

public BigDecimal getTotalCreditAmount() {
    return totalCreditAmount;
}
public void setTotalCreditAmount(BigDecimal totalCreditAmount) {
    this.totalCreditAmount = totalCreditAmount;
}

public BigDecimal getRefundAfterOutstandingBalance() {
    return refundAfterOutstandingBalance;
}
public void setRefundAfterOutstandingBalance(BigDecimal refundAfterOutstandingBalance) {
    this.refundAfterOutstandingBalance = refundAfterOutstandingBalance;
}

public LocalDate getWithdrawlFromDate() {
    return withdrawlFromDate;
}
public void setWithdrawlFromDate(LocalDate withdrawlFromDate) {
    this.withdrawlFromDate = withdrawlFromDate;
}

public LocalDate getWithdrawlToDate() {
    return withdrawlToDate;
}
public void setWithdrawlToDate(LocalDate withdrawlToDate) {
    this.withdrawlToDate = withdrawlToDate;
}

public BigDecimal getTotalWithdrawlAmount() {
    return totalWithdrawlAmount;
}
public void setTotalWithdrawlAmount(BigDecimal totalWithdrawlAmount) {
    this.totalWithdrawlAmount = totalWithdrawlAmount;
}

}
