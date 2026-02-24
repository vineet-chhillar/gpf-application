package com.example.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "gpfwithdrawldetails")
public class GpfWithdrawlDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ================= BASIC DETAILS ================= */

    //@Column(name = "empcode", nullable = false, length = 20)
    //private String empcode;

    @Column(name = "gpfaccountno", nullable = false, length = 50)
    private String gpfaccountno;

    @Column(name = "basicpay", precision = 15, scale = 2)
    private BigDecimal basicpay;

    /* ================= OUTSTANDING ================= */

    @Column(name = "dateofoutstandingbalance")
    private LocalDate dateofoutstandingbalance;

    @Column(name = "outstandingbalance", precision = 15, scale = 2)
    private BigDecimal outstandingbalance;

    /* ================= CREDIT PERIOD ================= */

    @Column(name = "creditfromdate")
    private LocalDate creditfromdate;

    @Column(name = "credittodate")
    private LocalDate credittodate;

    @Column(name = "totalcreditamount", precision = 15, scale = 2)
    private BigDecimal totalcreditamount;

    @Column(name = "refundafterdateofoutstandingbalance", precision = 15, scale = 2)
    private BigDecimal refundafterdateofoutstandingbalance;

    /* ================= WITHDRAWAL PERIOD ================= */

    @Column(name = "withdrawlfromdate")
    private LocalDate withdrawlfromdate;

    @Column(name = "withdrawltodate")
    private LocalDate withdrawltodate;

    @Column(name = "totalwithdrawlamount", precision = 15, scale = 2)
    private BigDecimal totalwithdrawlamount;

    @Column(name = "netbalance", precision = 15, scale = 2)
    private BigDecimal netbalance;

    /* ================= REQUEST ================= */

    @Column(name = "amountofwithdrawlrequested", precision = 15, scale = 2)
    private BigDecimal amountofwithdrawlrequested;

    @Column(name = "purposeofwithdrawl", length = 500)
    private String purposeofwithdrawl;

    @Column(name = "withdrawlrule")
    private Long withdrawlrule;

    /* ================= PRIOR WITHDRAWAL ================= */

    @Column(name = "ispriorwithdrawlforsamepurpose")
    private Boolean ispriorwithdrawlforsamepurpose;

    @Column(name = "priorwithdrawlamount", precision = 15, scale = 2)
    private BigDecimal priorwithdrawlamount;

    @Column(name = "priorwithdrawlfinyear", length = 20)
    private String priorwithdrawlfinyear;

    /* ================= APPLICATION ================= */

    @Column(name = "dateofapplication")
    private LocalDate dateofapplication;

    /* ================= ACTION (Replaced Status) ================= */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", nullable = false)
    private ActionMaster action;

    /* ================= AUDIT ================= */

    @Column(name = "createdat", insertable = false, updatable = false)
    private LocalDateTime createdat;

    @Column(name = "current_owner_role", nullable = false)
    private Long currentOwnerRole;

    /* ================= WORKFLOW ================= */

@Column(name = "workflow_id")
private Long workflowId;

@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "application_id", nullable = false)
private GpfWithdrawlMaster master;

@Column(name = "current_step")
private Integer currentStep;

    /* ================= GETTERS & SETTERS ================= */

    public Long getCurrentOwnerRole() {
    return currentOwnerRole;
}

public void setCurrentOwnerRole(Long currentOwnerRole) {
    this.currentOwnerRole = currentOwnerRole;
}

public GpfWithdrawlMaster getMaster() {
    return master;
}

public void setMaster(GpfWithdrawlMaster master) {
    this.master = master;
}

    public Long getId() {
        return id;
    }

    //public String getEmpcode() {
      //  return empcode;
   // }

    //public void setEmpcode(String empcode) {
       // this.empcode = empcode;
   // }

    public String getGpfaccountno() {
        return gpfaccountno;
    }

    public void setGpfaccountno(String gpfaccountno) {
        this.gpfaccountno = gpfaccountno;
    }

    public BigDecimal getBasicpay() {
        return basicpay;
    }

    public void setBasicpay(BigDecimal basicpay) {
        this.basicpay = basicpay;
    }

    public LocalDate getDateofoutstandingbalance() {
        return dateofoutstandingbalance;
    }

    public void setDateofoutstandingbalance(LocalDate dateofoutstandingbalance) {
        this.dateofoutstandingbalance = dateofoutstandingbalance;
    }

    public BigDecimal getOutstandingbalance() {
        return outstandingbalance;
    }

    public void setOutstandingbalance(BigDecimal outstandingbalance) {
        this.outstandingbalance = outstandingbalance;
    }

    public LocalDate getCreditfromdate() {
        return creditfromdate;
    }

    public void setCreditfromdate(LocalDate creditfromdate) {
        this.creditfromdate = creditfromdate;
    }

    public LocalDate getCredittodate() {
        return credittodate;
    }

    public void setCredittodate(LocalDate credittodate) {
        this.credittodate = credittodate;
    }

    public BigDecimal getTotalcreditamount() {
        return totalcreditamount;
    }

    public void setTotalcreditamount(BigDecimal totalcreditamount) {
        this.totalcreditamount = totalcreditamount;
    }

    public BigDecimal getRefundafterdateofoutstandingbalance() {
        return refundafterdateofoutstandingbalance;
    }

    public void setRefundafterdateofoutstandingbalance(BigDecimal refundafterdateofoutstandingbalance) {
        this.refundafterdateofoutstandingbalance = refundafterdateofoutstandingbalance;
    }

    public LocalDate getWithdrawlfromdate() {
        return withdrawlfromdate;
    }

    public void setWithdrawlfromdate(LocalDate withdrawlfromdate) {
        this.withdrawlfromdate = withdrawlfromdate;
    }

    public LocalDate getWithdrawltodate() {
        return withdrawltodate;
    }

    public void setWithdrawltodate(LocalDate withdrawltodate) {
        this.withdrawltodate = withdrawltodate;
    }

    public BigDecimal getTotalwithdrawlamount() {
        return totalwithdrawlamount;
    }

    public void setTotalwithdrawlamount(BigDecimal totalwithdrawlamount) {
        this.totalwithdrawlamount = totalwithdrawlamount;
    }

    public BigDecimal getNetbalance() {
        return netbalance;
    }

    public void setNetbalance(BigDecimal netbalance) {
        this.netbalance = netbalance;
    }

    public BigDecimal getAmountofwithdrawlrequested() {
        return amountofwithdrawlrequested;
    }

    public void setAmountofwithdrawlrequested(BigDecimal amountofwithdrawlrequested) {
        this.amountofwithdrawlrequested = amountofwithdrawlrequested;
    }

    public String getPurposeofwithdrawl() {
        return purposeofwithdrawl;
    }

    public void setPurposeofwithdrawl(String purposeofwithdrawl) {
        this.purposeofwithdrawl = purposeofwithdrawl;
    }

    public Long getWithdrawlrule() {
        return withdrawlrule;
    }

    public void setWithdrawlrule(Long withdrawlrule) {
        this.withdrawlrule = withdrawlrule;
    }

    public Boolean getIspriorwithdrawlforsamepurpose() {
        return ispriorwithdrawlforsamepurpose;
    }

    public void setIspriorwithdrawlforsamepurpose(Boolean ispriorwithdrawlforsamepurpose) {
        this.ispriorwithdrawlforsamepurpose = ispriorwithdrawlforsamepurpose;
    }

    public BigDecimal getPriorwithdrawlamount() {
        return priorwithdrawlamount;
    }

    public void setPriorwithdrawlamount(BigDecimal priorwithdrawlamount) {
        this.priorwithdrawlamount = priorwithdrawlamount;
    }

    public String getPriorwithdrawlfinyear() {
        return priorwithdrawlfinyear;
    }

    public void setPriorwithdrawlfinyear(String priorwithdrawlfinyear) {
        this.priorwithdrawlfinyear = priorwithdrawlfinyear;
    }

    public LocalDate getDateofapplication() {
        return dateofapplication;
    }

    public void setDateofapplication(LocalDate dateofapplication) {
        this.dateofapplication = dateofapplication;
    }

    public ActionMaster getAction() {
        return action;
    }

    public void setAction(ActionMaster action) {
        this.action = action;
    }

    public LocalDateTime getCreatedat() {
        return createdat;
    }

  

    

    public Long getWorkflowId() {
    return workflowId;
}

public void setWorkflowId(Long workflowId) {
    this.workflowId = workflowId;
}
public Integer getCurrentStep() {
    return currentStep;
}

public void setCurrentStep(Integer currentStep) {
    this.currentStep = currentStep;
}

}
