package com.example.backend.dto;

public class WorkflowTransitionViewDTO {

    private Long transitionId;
    private Integer stepOrder;
    private String fromRole;
    private String toRole;
    private Boolean isFinal;

private Long roleId;
private String roleName;

public Long getRoleId() {
    return roleId;
}

public void setRoleId(Long roleId) {
    this.roleId = roleId;
}

public String getRoleName() {
    return roleName;
}

public void setRoleName(String roleName) {
    this.roleName = roleName;
}

    // getters setters
    public Long getTransitionId() {
        return transitionId;
    }

    public void setTransitionId(Long transitionId) {
        this.transitionId = transitionId;
    }

    public Integer getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(Integer stepOrder) {
        this.stepOrder = stepOrder;
    }

    public String getFromRole() {
        return fromRole;
    }

    public void setFromRole(String fromRole) {
        this.fromRole = fromRole;
    }

    public String getToRole() {
        return toRole;
    }

    public void setToRole(String toRole) {
        this.toRole = toRole;
    }

    public Boolean getIsFinal() {
        return isFinal;
    }

    public void setIsFinal(Boolean isFinal) {
        this.isFinal = isFinal;
    }
}
