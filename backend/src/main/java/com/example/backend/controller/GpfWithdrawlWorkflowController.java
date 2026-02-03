package com.example.backend.controller;

import com.example.backend.dto.AdminInboxDTO;
import com.example.backend.dto.ApplicationDetailsDTO;
import com.example.backend.service.GpfWithdrawlWorkflowService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.backend.dto.EmployeeInboxDTO;
import com.example.backend.dto.ApplicationStatusTrailDTO;   
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
@PostMapping("/gpf/withdrawal/{id}/cash-verify")
public ResponseEntity<?> cashVerify(
        @PathVariable Long id,
        @RequestBody Map<String, String> body) {

    service.cashVerify(id, body.get("remarks"));
    return ResponseEntity.ok("Application verified by CASH and sent back to ADMIN");
}

    /* ================= ADMIN INBOX ================= */

   @GetMapping("/gpf/withdrawal/inbox")
public List<AdminInboxDTO> getInbox(@RequestParam String role) {

    if ("ADMIN".equalsIgnoreCase(role)) {
        return service.getAdminInbox();
    }

    if ("CASH".equalsIgnoreCase(role)) {
        return service.getCashInbox();
    }

    throw new IllegalArgumentException("Unsupported role: " + role);
}


    /* ================= APPLICATION DETAILS ================= */

    @GetMapping("/gpf/withdrawal/{id}")
    public ApplicationDetailsDTO getDetails(@PathVariable Long id) {
        return service.getApplicationDetails(id);
    }
    @GetMapping("/gpf/withdrawal/employee/inbox")
public List<EmployeeInboxDTO> getEmployeeInbox(@RequestParam String empcode) {
    return service.getEmployeeInbox(empcode);
}
@GetMapping("/gpf/withdrawal/{id}/status-trail")
public List<ApplicationStatusTrailDTO> getStatusTrail(@PathVariable Long id) {
    return service.getStatusTrail(id);
}

}
