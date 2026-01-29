package com.example.backend.dto;
import com.example.backend.entity.GpfWithdrawlDetails;
import com.example.backend.entity.GpfWithdrawlMaster;

import jakarta.validation.constraints.NotNull;


public class GpfWithdrawlRequestDTO {

    public GpfWithdrawlRequestDTO() {
}
    @NotNull
    private GpfWithdrawlMaster master;
    
    @NotNull
    private GpfWithdrawlDetails details;

    public GpfWithdrawlMaster getMaster() {
        return master;
    }

    public void setMaster(GpfWithdrawlMaster master) {
        this.master = master;
    }

    public GpfWithdrawlDetails getDetails() {
        return details;
    }

    public void setDetails(GpfWithdrawlDetails details) {
        this.details = details;
    }
}



