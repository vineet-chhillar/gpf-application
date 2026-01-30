package com.example.backend.repository;
import com.example.backend.entity.GpfWithdrawlRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import jakarta.transaction.Transactional;
import java.util.List;
public interface GpfWithdrawlRuleRepository
        extends JpaRepository<GpfWithdrawlRule, Long> {

    List<GpfWithdrawlRule> findByIsActiveTrue();
List<GpfWithdrawlRule> findAllByOrderByRuleCodeAsc();

   


@Modifying
@Transactional
 @Query("update GpfWithdrawlRule r set r.isActive = :active where r.ruleId = :id")
void updateActiveStatus(Long id, Boolean active);

}
