package com.example.backend.repository;

import com.example.backend.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;

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
}
