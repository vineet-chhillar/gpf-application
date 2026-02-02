package com.example.backend.controller;

import com.example.backend.entity.GpfWithdrawlRule;
import com.example.backend.service.GpfWithdrawlRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gpf")
@CrossOrigin
public class GpfWithdrawlRuleController {

    @Autowired
    private GpfWithdrawlRuleService service;

    /* ================= RULE MASTER (ALL RULES) ================= */
    @GetMapping("/withdrawal-rules")
    public List<GpfWithdrawlRule> getAllRules() {
        return service.getAllRules();   // ✅ ALL rules
    }

    /* ================= TRANSACTIONAL (ACTIVE ONLY) ================= */
    @GetMapping("/withdrawal-rules/active")
    public List<GpfWithdrawlRule> getActiveRules() {
        return service.getActiveRules(); // ✅ ACTIVE only
    }

    /* ================= TOGGLE ACTIVE ================= */
    @PatchMapping("/withdrawal-rules/{id}/toggle")
    public void toggleRule(@PathVariable Long id) {
        service.toggleActive(id);
    }

    
    /* ================= CREATE RULE ================= */
   @PostMapping("/withdrawal-rules")
public ResponseEntity<?> createRule(@RequestBody GpfWithdrawlRule rule) {
    try {
        GpfWithdrawlRule saved = service.saveRule(rule);
        return ResponseEntity.ok(saved);
    } catch (IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ex.getMessage());
    }
}
}
