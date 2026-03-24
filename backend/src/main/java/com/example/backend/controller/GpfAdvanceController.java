package com.example.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.AdvanceRequestDTO;
import com.example.backend.dto.ApplicationTrailDTO;
import com.example.backend.dto.GpfApplicationStatusResponseDTO;
import com.example.backend.dto.InboxApplicationDTO;
import com.example.backend.dto.WorkflowProcessRequestDTO;
import com.example.backend.service.GpfAdvanceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/gpf-advance")
public class GpfAdvanceController {

@Autowired
private GpfAdvanceService service;

@GetMapping("/status-all")
public List<GpfApplicationStatusResponseDTO> getAllApplicationStatus() {
    return service.getAllApplicationStatus();
}
    
@PostMapping("/save")
public ResponseEntity<?> saveAdvance(@RequestBody AdvanceRequestDTO dto) {

    try {

        System.out.println("===== SAVE ADVANCE API HIT =====");

        if (dto.getDetails() == null) {
            return ResponseEntity.badRequest().body("Details cannot be null");
        }

        System.out.println("MASTER : " + dto.getMaster());
        System.out.println("DETAILS : " + dto.getDetails());
        System.out.println("ADVANCE RULE : " + dto.getDetails().getAdvancerule());
         System.out.println("RULE DATA : " + dto.getRuleSpecificData());

        service.saveAdvanceApplication(
                dto.getMaster(),
                dto.getDetails(),
                 dto.getRuleSpecificData(),
                dto.getRoleId(),
                dto.getActionId());

        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Advance application saved"
        ));

    } catch (Exception e) {

        e.printStackTrace();

        return ResponseEntity.status(500).body(Map.of(
            "status", "error",
            "message", e.getMessage()
        ));
    }
}
 @GetMapping("/inbox")
    public List<InboxApplicationDTO> getInbox() {
        System.out.println("Advance inbox size = " );
        return service.getAllPendingApplications();
    }

    /* ================= PROCESS ================= */

    @PostMapping("/process")
    public void processApplications(
            @RequestBody WorkflowProcessRequestDTO request) {

        service.processApplications(request);
    }

    /* ================= STATUS ================= */

    @GetMapping("/status/{empcode}")
    public GpfApplicationStatusResponseDTO getStatus(
            @PathVariable String empcode) {

        return service.getApplicationStatus(empcode);
    }

    /* ================= TRAIL ================= */

    @GetMapping("/trail/{applicationId}")
    public List<ApplicationTrailDTO> getTrail(
            @PathVariable Long applicationId) {

        return service.getTrail(applicationId);
    }
    @PutMapping("/update/{id}")
public ResponseEntity<?> updateAdvance(
        @PathVariable Long id,
        @RequestBody AdvanceRequestDTO dto) {

    try {

        service.updateAdvanceApplication(
                id,
                dto.getDetails(),
                dto.getRuleSpecificData()
        );

        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Advance updated successfully"
        ));

    } catch (Exception e) {

        e.printStackTrace();

        return ResponseEntity.status(500).body(Map.of(
            "status", "error",
            "message", e.getMessage()
        ));
    }
}
}