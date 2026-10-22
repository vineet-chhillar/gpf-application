package com.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "applicationstatustrail", schema = "gpf")
public class ApplicationStatusTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "applicationid", nullable = false)
    private Long applicationId;


    @Column(name = "action_id", nullable = false)
    private Long actionId;

    @Column(name = "remarks")
    private String remarks;

   @Column(name = "action_by_role")
private Long actionByRole;


    @Column(name = "actionat", insertable = false, updatable = false)
    private LocalDateTime actionat;

    @Column(name="action_by_div_code")
    private String action_by_div_code;

    @Column(name="action_by_emp_code")
    private String action_by_emp_code;

    // ===== GETTERS & SETTERS =====

    public Long getId() {
        return id;
    }

   public Long getApplicationId() {
    return applicationId;
}

public void setApplicationId(Long applicationId) {
    this.applicationId = applicationId;
}


    public Long getActionId() {
        return actionId;
    }

    public void setActionId(Long actionId) {
        this.actionId = actionId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Long getActionByRole() {
    return actionByRole;
}

public void setActionByRole(Long actionByRole) {
    this.actionByRole = actionByRole;
}


public String getActionByDivCode()
{
    return action_by_div_code;
}
public void setActionByDivCode(String action_by_div_code)
{
    this.action_by_div_code=action_by_div_code;
}

public String getActionByEmpCode()
{
    return action_by_emp_code;
}
public void setActionByEmpCode(String action_by_emp_code)
{
    this.action_by_emp_code=action_by_emp_code;
}



    public LocalDateTime getActionat() {
        return actionat;
    }

    
}

