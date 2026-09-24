package com.example.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;


@Entity
@Table(name = "gpf_sanction_order")
public class GpfSanctionOrder {

     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 1
    @Column(name = "application_id")
    private Long applicationId;

    // 2
    @Column(name = "generated_by")
    private String generatedBy;

    // 3
    @Column(name = "generated_on")
    private LocalDateTime generatedOn;

    // 4
    @Column(name = "order_number")
    private String orderNumber;

    // 5  ⚠️ KEEP HERE
 @Lob
@Column(name = "order_pdf")
@org.hibernate.annotations.JdbcTypeCode(java.sql.Types.BINARY)
private byte[] orderPdf;

    // 6
    @Column(name = "order_text", columnDefinition = "TEXT")
    private String orderText;

    
    // ================= GETTERS & SETTERS =================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getOrderText() {
        return orderText;
    }

    public void setOrderText(String orderText) {
        this.orderText = orderText;
    }

    public byte[] getOrderPdf() {
        return orderPdf;
    }

    public void setOrderPdf(byte[] orderPdf) {
        this.orderPdf = orderPdf;
    }

    public LocalDateTime getGeneratedOn() {
        return generatedOn;
    }

    public void setGeneratedOn(LocalDateTime generatedOn) {
        this.generatedOn = generatedOn;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(String generatedBy) {
        this.generatedBy = generatedBy;
    }
}
