package com.example.backend.dto;

public class GenerateOrderRequest  {
    private Long applicationId;
     private String type; // "withdrawl" or "advance"


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    
}
