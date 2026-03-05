package com.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "advanceapplicationstatustrail")
public class AdvanceApplicationStatusTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long applicationid;

    @Column(name = "action_id")
    private Long actionId;

    private String remarks;

    private LocalDateTime actionat;

    @Column(name = "action_by_role")
    private Long actionByRole;

    // ===== GETTERS =====

    public Long getId() {
        return id;
    }

    public Long getApplicationid() {
        return applicationid;
    }

    public Long getActionId() {
        return actionId;
    }

    public String getRemarks() {
        return remarks;
    }

    public LocalDateTime getActionat() {
        return actionat;
    }

    public Long getActionByRole() {
        return actionByRole;
    }

    // ===== SETTERS =====

    public void setId(Long id) {
        this.id = id;
    }

    public void setApplicationid(Long applicationid) {
        this.applicationid = applicationid;
    }

    public void setActionId(Long actionId) {
        this.actionId = actionId;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void setActionat(LocalDateTime actionat) {
        this.actionat = actionat;
    }

    public void setActionByRole(Long actionByRole) {
        this.actionByRole = actionByRole;
    }
}