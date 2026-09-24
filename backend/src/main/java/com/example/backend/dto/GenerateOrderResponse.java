package com.example.backend.dto;

public class GenerateOrderResponse {
     private Long orderId;
    private String orderNumber;
    private String status;
    private String message;

    private SanctionOrderDto orderData;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public SanctionOrderDto getOrderData() {
        return orderData;
    }

    public void setOrderData(SanctionOrderDto orderData) {
        this.orderData = orderData;
    }
}
