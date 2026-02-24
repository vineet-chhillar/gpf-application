package com.example.backend.dto;

import java.util.List;

public class GpfApplicationStatusResponseDTO {

    private Object master;
    private Object details;
    private List<ApplicationTrailDTO> trail;

    public Object getMaster() {
        return master;
    }

    public void setMaster(Object master) {
        this.master = master;
    }

    public Object getDetails() {
        return details;
    }

    public void setDetails(Object details) {
        this.details = details;
    }

    public List<ApplicationTrailDTO> getTrail() {
        return trail;
    }

    public void setTrail(List<ApplicationTrailDTO> trail) {
        this.trail = trail;
    }
}