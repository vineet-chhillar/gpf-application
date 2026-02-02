package com.example.backend.service;
import com.example.backend.entity.GpfWithdrawlRule;
import com.example.backend.repository.GpfWithdrawlRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

@Service
public class GpfWithdrawlRuleService {

    @Autowired
    private GpfWithdrawlRuleRepository repo;

    public List<GpfWithdrawlRule> getActiveRules() {
        return repo.findByIsActiveTrue();
    }

    public List<GpfWithdrawlRule> getAllRules() {
    return repo.findAllByOrderByRuleCodeAsc();
}


   public GpfWithdrawlRule saveRule(GpfWithdrawlRule rule) {

    // ✅ Pre-check to give clean message
    if (repo.existsByRuleCode(rule.getRuleCode())) {
        throw new IllegalStateException("Rule Code already exists");
    }

    try {
        return repo.save(rule);
    } catch (DataIntegrityViolationException ex) {
        // ✅ Safety net (race condition)
        throw new IllegalStateException("Rule Code already exists");
    }
}

    

    @Transactional
public void toggleActive(Long id) {
    GpfWithdrawlRule rule = repo.findById(id)
        .orElseThrow(() -> new RuntimeException("Rule not found"));

    rule.setIsActive(!Boolean.TRUE.equals(rule.getIsActive()));
    repo.save(rule);

}

}
