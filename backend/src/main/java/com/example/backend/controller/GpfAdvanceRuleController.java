package com.example.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.entity.GpfAdvanceRule;
import com.example.backend.service.GpfAdvanceRuleService;

@RestController
@RequestMapping("/api/gpf/advance-rules")
@CrossOrigin
public class GpfAdvanceRuleController {

    @Autowired
    private GpfAdvanceRuleService service;

    @GetMapping
    public List<GpfAdvanceRule> getAll() {
        return service.getAll();
    }
@GetMapping("/active")
public ResponseEntity<?> getActiveRules() {

    System.out.println("ACTIVE API HIT");

    try {

        List<GpfAdvanceRule> list = service.getActiveRules();

        System.out.println("DATA SIZE: " + list.size());

        return ResponseEntity.ok(list);

    } catch (Exception e) {

        e.printStackTrace();

        return ResponseEntity.status(500).body(e.getMessage());
    }
}
    @PostMapping
    public ResponseEntity<?> save(@RequestBody GpfAdvanceRule rule) {

        try {
            return ResponseEntity.ok(service.save(rule));
        }
        catch (RuntimeException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }

    }

   @PatchMapping("/{ruleId}/toggle")
public ResponseEntity<?> toggleRule(@PathVariable Long ruleId) {

    service.toggleRule(ruleId);

    return ResponseEntity.ok("Rule status updated");
}
}
