package com.example.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "workflow_transition")
public class WorkflowTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transitionId;

    @Column(name = "workflow_id", nullable = false)
    private Long workflowId;

    @Column(name = "from_role", nullable = false)
private Long fromRole;

@Column(name = "to_role", nullable = false)
private Long toRole;


    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "is_final")
    private Boolean isFinal;

    // Getters & Setters

    public Long getTransitionId() {
        return transitionId;
    }

    public Long getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(Long workflowId) {
        this.workflowId = workflowId;
    }

    public long getFromRole() {
        return fromRole;
    }

    public void setFromRole(long fromRole) {
        this.fromRole = fromRole;
    }

    public long getToRole() {
        return toRole;
    }

    public void setToRole(long toRole) {
        this.toRole = toRole;
    }

    public Integer getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(Integer stepOrder) {
        this.stepOrder = stepOrder;
    }

    public Boolean getIsFinal() {
        return isFinal;
    }

    public void setIsFinal(Boolean isFinal) {
        this.isFinal = isFinal;
    }
}
