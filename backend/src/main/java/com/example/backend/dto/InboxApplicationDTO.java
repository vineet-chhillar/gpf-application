package com.example.backend.dto;

import java.math.BigDecimal;

import com.example.backend.entity.GpfWithdrawlMaster;

public class InboxApplicationDTO {

    private Long applicationId;
    private String employeeName;
    private String empCode;
    private BigDecimal amount;
    private String applicationDate;
    private String pendingWithRole;
    private String designation;
private String purpose;


public String getDesignation() {
    return designation;
}

public void setDesignation(String designation) {
    this.designation = designation;
}

public String getPurpose() {
    return purpose;
}

public void setPurpose(String purpose) {
    this.purpose = purpose;
}
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

     public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
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