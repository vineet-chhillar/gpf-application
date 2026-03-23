package com.example.backend.dto;

import java.util.List;

public class WorkflowProcessRequestDTO  {

    private List<Long> applicationIds;
    private Long actionId;
    private Long roleId;
    private String remarks;

    // 🔥 ADD THIS
    private Long sendToRole;

    public List<Long> getApplicationIds() {
        return applicationIds;
    }

    public void setApplicationIds(List<Long> applicationIds) {
        this.applicationIds = applicationIds;
    }

    public Long getActionId() {
        return actionId;
    }

    public void setActionId(Long actionId) {
        this.actionId = actionId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    // 🔥 NEW GETTER
    public Long getSendToRole() {
        return sendToRole;
    }

    // 🔥 NEW SETTER
    public void setSendToRole(Long sendToRole) {
        this.sendToRole = sendToRole;
    }
}