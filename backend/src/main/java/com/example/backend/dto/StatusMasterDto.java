package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class StatusMasterDto {

    @NotBlank(message = "Status code is required")
    @Size(max = 50, message = "Status code must not exceed 50 characters")
    private String statusCode;

    @NotNull(message = "Final flag is required")
    private Boolean isFinal;

    @NotNull(message = "Active flag is required")
    private Boolean isActive;

    // getters & setters

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public Boolean getIsFinal() {
        return isFinal;
    }

    public void setIsFinal(Boolean isFinal) {
        this.isFinal = isFinal;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
