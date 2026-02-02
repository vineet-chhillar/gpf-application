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
}
