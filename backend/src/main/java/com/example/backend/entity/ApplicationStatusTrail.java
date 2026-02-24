package com.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "applicationstatustrail")
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


    public LocalDateTime getActionat() {
        return actionat;
    }

    
}

