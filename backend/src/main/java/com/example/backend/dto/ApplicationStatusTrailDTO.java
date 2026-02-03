package com.example.backend.dto;

import java.time.LocalDateTime;

public class ApplicationStatusTrailDTO {

    private String statusCode;
    private String actionBy;
    private String remarks;
    private LocalDateTime actionAt;

    // ===== GETTERS & SETTERS =====

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getActionBy() {
        return actionBy;
    }

    public void setActionBy(String actionBy) {
        this.actionBy = actionBy;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getActionAt() {
        return actionAt;
    }

    public void setActionAt(LocalDateTime actionAt) {
        this.actionAt = actionAt;
    }
}
