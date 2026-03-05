package com.example.backend.dto;


import com.example.backend.entity.GpfAdvanceMaster;
import com.example.backend.entity.GpfAdvanceDetails;

public class AdvanceRequestDTO {

    private GpfAdvanceMaster master;
    private GpfAdvanceDetails details;

    private Long roleId;
    private Long actionId;

    /* ================= GETTERS ================= */

    public GpfAdvanceMaster getMaster() {
        return master;
    }

    public GpfAdvanceDetails getDetails() {
        return details;
    }

    public Long getRoleId() {
        return roleId;
    }

    public Long getActionId() {
        return actionId;
    }

    /* ================= SETTERS ================= */

    public void setMaster(GpfAdvanceMaster master) {
        this.master = master;
    }

    public void setDetails(GpfAdvanceDetails details) {
        this.details = details;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public void setActionId(Long actionId) {
        this.actionId = actionId;
    }
}
