package com.example.backend.dto;

import java.time.LocalDateTime;

public class WorkflowMasterDTO {

    private Long workflowId;
    private String workflowName;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String workflowDiv;
    private String divName;

    public Long getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(Long workflowId) {
        this.workflowId = workflowId;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getWorkflowDiv() {
        return workflowDiv;
    }

    public void setWorkflowDiv(String workflowDiv) {
        this.workflowDiv = workflowDiv;
    }

    public String getDivName() {
        return divName;
    }

    public void setDivName(String divName) {
        this.divName = divName;
    }
}

