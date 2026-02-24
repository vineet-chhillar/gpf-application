package com.example.backend.dto;

import com.example.backend.entity.GpfWithdrawlMaster;

public class InboxApplicationDTO {

    private Long applicationId;
    private String employeeName;
    private String empCode;
    private Double amount;
    private String applicationDate;
    private String pendingWithRole;

    public InboxApplicationDTO() {
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmpCode() {
        return empCode;
    }

    public void setEmpCode(String empCode) {
        this.empCode = empCode;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(String applicationDate) {
        this.applicationDate = applicationDate;
    }

    public String getPendingWithRole() {
        return pendingWithRole;
    }

    public void setPendingWithRole(String pendingWithRole) {
        this.pendingWithRole = pendingWithRole;
    }

    
}