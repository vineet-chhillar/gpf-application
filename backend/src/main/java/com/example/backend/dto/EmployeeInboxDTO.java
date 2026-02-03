package com.example.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EmployeeInboxDTO {

    private Long applicationId;
    private LocalDate dateOfApplication;
    private BigDecimal amountRequested;
    private String statusCode;
    private String currentOwnerRole;
    private Boolean isFinal;
    private String empCode;

    // ===== GETTERS & SETTERS =====

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public LocalDate getDateOfApplication() {
        return dateOfApplication;
    }
    public String getEmpCode() {
        return empCode;
    }

    public void setEmpCode(String empCode) {
        this.empCode = empCode;
    }

    public void setDateOfApplication(LocalDate dateOfApplication) {
        this.dateOfApplication = dateOfApplication;
    }

    public BigDecimal getAmountRequested() {
        return amountRequested;
    }

    public void setAmountRequested(BigDecimal amountRequested) {
        this.amountRequested = amountRequested;
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

    public Boolean getIsFinal() {
        return isFinal;
    }

    public void setIsFinal(Boolean isFinal) {
        this.isFinal = isFinal;
    }
}
