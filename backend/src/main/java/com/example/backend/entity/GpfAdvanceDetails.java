package com.example.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "gpfadvancedetails")
public class GpfAdvanceDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gpfaccountno;

    private BigDecimal basicpay;

    private LocalDate dateofoutstandingbalance;
    private BigDecimal outstandingbalance;

    private LocalDate creditfromdate;
    private LocalDate credittodate;
    private BigDecimal totalcreditamount;

    private BigDecimal refundafterdateofoutstandingbalance;

    private BigDecimal amountofadvanceoutstanding;
    private BigDecimal amountofconsolidatedadvance;
    private Integer noofmonthlyinstallmentsforpaymentofconsolidatedadvance;

    private BigDecimal amountofadvancerequested;
    private String purposeofadvance;

    private Long advancerule;

    private LocalDate dateofapplication;
    private LocalDate withdrawlfromdate;
private LocalDate withdrawltodate;
private BigDecimal totalwithdrawlamount;
private BigDecimal netbalance;

 @Column(name = "action_id")
private Long actionId;

    private LocalDateTime createdat;

    @Column(name = "workflow_id")
    private Long workflowId;

    @Column(name = "application_id")
    private Long applicationId;

    @Column(name = "current_owner_role")
    private Long currentOwnerRole;

    @Column(name = "current_step")
    private Integer currentStep;

    // ===== GETTERS =====

    public LocalDate getWithdrawlfromdate() {
    return withdrawlfromdate;
}
public Long getActionId() {
    return actionId;
}
public LocalDate getWithdrawltodate() {
    return withdrawltodate;
}

public BigDecimal getTotalwithdrawlamount() {
    return totalwithdrawlamount;
}

public BigDecimal getNetbalance() {
    return netbalance;
}

    public Long getId() {
        return id;
    }

    public String getGpfaccountno() {
        return gpfaccountno;
    }

    public BigDecimal getBasicpay() {
        return basicpay;
    }

    public LocalDate getDateofoutstandingbalance() {
        return dateofoutstandingbalance;
    }

    public BigDecimal getOutstandingbalance() {
        return outstandingbalance;
    }

    public LocalDate getCreditfromdate() {
        return creditfromdate;
    }

    public LocalDate getCredittodate() {
        return credittodate;
    }

    public BigDecimal getTotalcreditamount() {
        return totalcreditamount;
    }

    public BigDecimal getRefundafterdateofoutstandingbalance() {
        return refundafterdateofoutstandingbalance;
    }

    public BigDecimal getAmountofadvanceoutstanding() {
        return amountofadvanceoutstanding;
    }

    public BigDecimal getAmountofconsolidatedadvance() {
        return amountofconsolidatedadvance;
    }

    public Integer getNoofmonthlyinstallmentsforpaymentofconsolidatedadvance() {
        return noofmonthlyinstallmentsforpaymentofconsolidatedadvance;
    }

    public BigDecimal getAmountofadvancerequested() {
        return amountofadvancerequested;
    }

    public String getPurposeofadvance() {
        return purposeofadvance;
    }

    public Long getAdvancerule() {
        return advancerule;
    }

    public LocalDate getDateofapplication() {
        return dateofapplication;
    }

    //public ActionMaster getAction() {
    //return action;
//}

    public LocalDateTime getCreatedat() {
        return createdat;
    }

    public Long getWorkflowId() {
        return workflowId;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public Long getCurrentOwnerRole() {
        return currentOwnerRole;
    }

    public Integer getCurrentStep() {
        return currentStep;
    }

    // ===== SETTERS =====
    public void setWithdrawlfromdate(LocalDate withdrawlfromdate) {
    this.withdrawlfromdate = withdrawlfromdate;
}

public void setActionId(Long actionId) {
    this.actionId = actionId;
}

public void setWithdrawltodate(LocalDate withdrawltodate) {
    this.withdrawltodate = withdrawltodate;
}

public void setTotalwithdrawlamount(BigDecimal totalwithdrawlamount) {
    this.totalwithdrawlamount = totalwithdrawlamount;
}


public void setNetbalance(BigDecimal netbalance) {
    this.netbalance = netbalance;
}


    public void setId(Long id) {
        this.id = id;
    }

    public void setGpfaccountno(String gpfaccountno) {
        this.gpfaccountno = gpfaccountno;
    }

    public void setBasicpay(BigDecimal basicpay) {
        this.basicpay = basicpay;
    }

    public void setDateofoutstandingbalance(LocalDate dateofoutstandingbalance) {
        this.dateofoutstandingbalance = dateofoutstandingbalance;
    }

    public void setOutstandingbalance(BigDecimal outstandingbalance) {
        this.outstandingbalance = outstandingbalance;
    }

    public void setCreditfromdate(LocalDate creditfromdate) {
        this.creditfromdate = creditfromdate;
    }

    public void setCredittodate(LocalDate credittodate) {
        this.credittodate = credittodate;
    }

    public void setTotalcreditamount(BigDecimal totalcreditamount) {
        this.totalcreditamount = totalcreditamount;
    }

    public void setRefundafterdateofoutstandingbalance(BigDecimal refundafterdateofoutstandingbalance) {
        this.refundafterdateofoutstandingbalance = refundafterdateofoutstandingbalance;
    }

    public void setAmountofadvanceoutstanding(BigDecimal amountofadvanceoutstanding) {
        this.amountofadvanceoutstanding = amountofadvanceoutstanding;
    }

    public void setAmountofconsolidatedadvance(BigDecimal amountofconsolidatedadvance) {
        this.amountofconsolidatedadvance = amountofconsolidatedadvance;
    }

    public void setNoofmonthlyinstallmentsforpaymentofconsolidatedadvance(Integer noofmonthlyinstallmentsforpaymentofconsolidatedadvance) {
        this.noofmonthlyinstallmentsforpaymentofconsolidatedadvance = noofmonthlyinstallmentsforpaymentofconsolidatedadvance;
    }

    public void setAmountofadvancerequested(BigDecimal amountofadvancerequested) {
        this.amountofadvancerequested = amountofadvancerequested;
    }

    public void setPurposeofadvance(String purposeofadvance) {
        this.purposeofadvance = purposeofadvance;
    }

    public void setAdvancerule(Long advancerule) {
        this.advancerule = advancerule;
    }

    public void setDateofapplication(LocalDate dateofapplication) {
        this.dateofapplication = dateofapplication;
    }

   

    public void setCreatedat(LocalDateTime createdat) {
        this.createdat = createdat;
    }

    public void setWorkflowId(Long workflowId) {
        this.workflowId = workflowId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public void setCurrentOwnerRole(Long currentOwnerRole) {
        this.currentOwnerRole = currentOwnerRole;
    }

    public void setCurrentStep(Integer currentStep) {
        this.currentStep = currentStep;
    }
}