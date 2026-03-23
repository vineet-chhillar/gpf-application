package com.example.backend.dto;

import java.util.List;

public class GpfApplicationStatusResponseDTO {

    private Object master;
    private Object details;
    private List<ApplicationTrailDTO> trail;
private String lastRemarks;
private String lastActionByRole;
private String currentOwnerRole;
private Long currentOwnerRoleId;

public Long getCurrentOwnerRoleId() {
    return currentOwnerRoleId;
}

public void setCurrentOwnerRoleId(Long currentOwnerRoleId) {
    this.currentOwnerRoleId = currentOwnerRoleId;
}
    public Object getMaster() {
        return master;
    }

    public void setMaster(Object master) {
        this.master = master;
    }

    public Object getDetails() {
        return details;
    }

    public void setDetails(Object details) {
        this.details = details;
    }

    public List<ApplicationTrailDTO> getTrail() {
        return trail;
    }

    public void setTrail(List<ApplicationTrailDTO> trail) {
        this.trail = trail;
    }
public String getLastRemarks() {
    return lastRemarks;
}

public void setLastRemarks(String lastRemarks) {
    this.lastRemarks = lastRemarks;
}

public String getLastActionByRole() {
    return lastActionByRole;
}

public void setLastActionByRole(String lastActionByRole) {
    this.lastActionByRole = lastActionByRole;
}

public String getCurrentOwnerRole() {
    return currentOwnerRole;
}

public void setCurrentOwnerRole(String currentOwnerRole) {
    this.currentOwnerRole = currentOwnerRole;
}
    
}