package com.example.backend.controller;

import com.example.backend.entity.GpfWithdrawlRule;
import com.example.backend.service.GpfWithdrawlRuleService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public GpfWithdrawlRule createRule(@RequestBody GpfWithdrawlRule rule) {
        return service.saveRule(rule);
    }
}
