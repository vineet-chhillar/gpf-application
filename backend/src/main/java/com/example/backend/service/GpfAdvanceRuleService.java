package com.example.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.entity.GpfAdvanceRule;
import com.example.backend.repository.GpfAdvanceRuleRepo;

import jakarta.transaction.Transactional;

@Service
public class GpfAdvanceRuleService {

    @Autowired
    private GpfAdvanceRuleRepo repo;

    public List<GpfAdvanceRule> getAll() {
        return repo.findAll();
    }

    public GpfAdvanceRule save(GpfAdvanceRule rule) {

        if (repo.findByRuleCode(rule.getRuleCode()).isPresent()) {
            throw new RuntimeException("Rule Code already exists");
        }

        rule.setCreatedat(LocalDateTime.now());
        rule.setCreatedby("ADMIN");

        return repo.save(rule);
    }


public List<GpfAdvanceRule> getActiveRules() {
    return repo.findByActiveTrue();
}

@Transactional
public void toggleRule(Long ruleId) {

    GpfAdvanceRule rule = repo.findById(ruleId)
            .orElseThrow(() -> new RuntimeException("Rule not found"));

    rule.setIsActive(!rule.getIsActive());

    repo.save(rule);
}
}
