package com.example.backend.controller;

import com.example.backend.dto.AdminInboxDTO;
import com.example.backend.dto.ApplicationDetailsDTO;
import com.example.backend.service.GpfWithdrawlWorkflowService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class GpfWithdrawlWorkflowController {

    private final GpfWithdrawlWorkflowService service;

    @Autowired
    public GpfWithdrawlWorkflowController(GpfWithdrawlWorkflowService service) {
        this.service = service;
    }

    /* ================= ADMIN VERIFY ================= */

    @PostMapping("/gpf/withdrawal/{id}/admin-verify")
    public ResponseEntity<?> adminVerify(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        service.adminVerify(id, body.get("remarks"));
        return ResponseEntity.ok("Application verified by ADMIN");
    }

    /* ================= ADMIN INBOX ================= */

    @GetMapping("/gpf/withdrawal/inbox")
    public List<AdminInboxDTO> getInbox(@RequestParam String role) {

        if ("ADMIN".equalsIgnoreCase(role)) {
            return service.getAdminInbox();
        }

        throw new IllegalArgumentException("Unsupported role: " + role);
    }

    /* ================= APPLICATION DETAILS ================= */

    @GetMapping("/gpf/withdrawal/{id}")
    public ApplicationDetailsDTO getDetails(@PathVariable Long id) {
        return service.getApplicationDetails(id);
    }
}
