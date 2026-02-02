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
    private Long applicationid;

    @Column(name = "statusid", nullable = false)
    private Long statusid;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "actionby", length = 100)
    private String actionby;

    @Column(name = "actionat", nullable = false, updatable = false)
    private LocalDateTime actionat;

    @PrePersist
    public void onCreate() {
        this.actionat = LocalDateTime.now();
    }

    /* ===== Getters & Setters ===== */

    public Long getId() {
        return id;
    }

    public Long getApplicationid() {
        return applicationid;
    }

    public void setApplicationid(Long applicationid) {
        this.applicationid = applicationid;
    }

    public Long getStatusid() {
        return statusid;
    }

    public void setStatusid(Long statusid) {
        this.statusid = statusid;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getActionby() {
        return actionby;
    }

    public void setActionby(String actionby) {
        this.actionby = actionby;
    }

    public LocalDateTime getActionat() {
        return actionat;
    }
}

