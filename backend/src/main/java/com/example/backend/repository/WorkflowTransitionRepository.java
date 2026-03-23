package com.example.backend.repository;

import com.example.backend.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkflowTransitionRepository
        extends JpaRepository<WorkflowTransition, Long> {

    Optional<WorkflowTransition>
findFirstByWorkflowIdAndFromRoleOrderByStepOrder(
        Long workflowId,
        Long fromRole
);

Optional<WorkflowTransition>
findFirstByWorkflowIdAndStepOrderGreaterThanOrderByStepOrder(
        Long workflowId,
        Integer stepOrder
);

Optional<WorkflowTransition>
    findFirstByWorkflowIdAndStepOrderOrderByStepOrder(
            Long workflowId,
            Integer stepOrder
    );
List<WorkflowTransition> findByWorkflowIdOrderByStepOrder(Long workflowId);

   List<WorkflowTransition> 
findByWorkflowIdAndStepOrderLessThanOrderByStepOrderDesc(
    Long workflowId,
    Integer stepOrder
);
@Query("""
    SELECT DISTINCT w.fromRole
    FROM WorkflowTransition w
    WHERE w.workflowId = :workflowId
      AND w.stepOrder < :stepOrder
    ORDER BY w.fromRole
""")
List<Long> findDistinctFromRoles(
    @Param("workflowId") Long workflowId,
    @Param("stepOrder") Integer stepOrder
);

Optional<WorkflowTransition> findByWorkflowIdAndStepOrder(
    Long workflowId,
    Integer stepOrder
);

}
