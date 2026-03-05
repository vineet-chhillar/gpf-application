package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.GpfAdvanceRule;

public interface GpfAdvanceRuleRepo extends JpaRepository<GpfAdvanceRule, Long> {

    Optional<GpfAdvanceRule> findByRuleCode(String ruleCode);

     List<GpfAdvanceRule> findByActiveTrue();
          

}
