package com.example.backend.dto;

import java.math.BigDecimal;

public class SanctionOrderDto {
    private Double requestedWithdrawlAmount;
    private String designation;
    private String empCode;
    private String empName;
    private String gpfAccNo;
    private String purposeOfWithdrawl;

    private String dateOfJoining;
    private String dateOfRetirement;

    private Double closingBalance;
    private Double creditAmount;
    private Double refundAmount;
    private Double subsequentWithdrawl;
    private Double requestedAdvanceAmount;
    private Double noOfInstallments;
    private Double installmentAmount;
    private String purposeOfAdvance;

    public Double getRequestedWithdrawlAmount() {
        return requestedWithdrawlAmount;
    }

    public void setRequestedWithdrawlAmount(Double requestedWithdrawlAmount) {
        this.requestedWithdrawlAmount = requestedWithdrawlAmount;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getEmpCode() {
        return empCode;
    }

    public void setEmpCode(String empCode) {
        this.empCode = empCode;
    }
    public String getEmpName() {
        return empName;
    }
    public void setEmpName(String empName) {
        this.empName = empName;
    }   
    

    public String getGpfAccNo() {
        return gpfAccNo;
    }

    public void setGpfAccNo(String gpfAccNo) {
        this.gpfAccNo = gpfAccNo;
    }

    public String getPurposeOfWithdrawl() {
        return purposeOfWithdrawl;
    }

    public void setPurposeOfWithdrawl(String purposeOfWithdrawl) {
        this.purposeOfWithdrawl = purposeOfWithdrawl;
    }

    public String getDateOfJoining() {
        return dateOfJoining;
    }

    public void setDateOfJoining(String dateOfJoining) {
        this.dateOfJoining = dateOfJoining;
    }

    public String getDateOfRetirement() {
        return dateOfRetirement;
    }

    public void setDateOfRetirement(String dateOfRetirement) {
        this.dateOfRetirement = dateOfRetirement;
    }

    public Double getClosingBalance() {
        return closingBalance;
    }

    public void setClosingBalance(Double closingBalance) {
        this.closingBalance = closingBalance;
    }

    public Double getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(Double creditAmount) {
        this.creditAmount = creditAmount;
    }

    public Double getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(Double refundAmount) {
        this.refundAmount = refundAmount;
    }

 public Double getSubsequentWithdrawl() {
    return subsequentWithdrawl != null ? subsequentWithdrawl : 0.0;
}

    public void setSubsequentWithdrawl(Double subsequentWithdrawl) {
        this.subsequentWithdrawl = subsequentWithdrawl;
    }

    public void setRequestedWithdrawlAmount(BigDecimal amountofwithdrawlrequested) {
        throw new UnsupportedOperationException("Unimplemented method 'setRequestedWithdrawlAmount'");
    }

    public Double getRequestedAdvanceAmount()
    {
        return requestedAdvanceAmount;
    }
    public void setRequestedAdvanceAmount(Double requestedAdvanceAmount)
    {
        this.requestedAdvanceAmount=requestedAdvanceAmount;
    }

    public Double getInstallmentAmount()
    {
        return installmentAmount;
    }
    public void setInstallmentAmount(Double installmentAmount)
    {
        this.installmentAmount=installmentAmount;
    }

    public Double getNoOfInstallments()
    {
        return noOfInstallments;
    }
    public void setNoOfInstallments(Double noOfInstallments)
    {
        this.noOfInstallments=noOfInstallments;
    }

    public String getPurposeOfAdvance() {
        return purposeOfAdvance;
    }
    public void setgetPurposeOfAdvance(String purposeOfAdvance) {
        this.purposeOfAdvance = purposeOfAdvance;
    }

}
