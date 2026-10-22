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
//List<WorkflowTransition> findByWorkflowIdOrderByStepOrder(Long workflowId);
@Query(value = """
    SELECT
        t.transition_id AS "transitionId",
        t.step_order AS "stepOrder",
        t.from_role AS "fromRole",
        t.to_role AS "toRole",
        t.is_final AS "isFinal",
        t.to_role_div_code AS "toRoleDivCode",
        d.div_name AS "divName"
    FROM gpf.workflow_transition t
    LEFT JOIN public.div_mast d
        ON t.to_role_div_code = d.div_code
    WHERE t.workflow_id = :workflowId
    ORDER BY t.step_order
    """, nativeQuery = true)
List<WorkflowTransitionProjection> findWorkflowTransitionsWithDivision(
        @Param("workflowId") Long workflowId);

interface WorkflowTransitionProjection {
    Long getTransitionId();
    Integer getStepOrder();
    Long getFromRole();
    Long getToRole();
    Boolean getIsFinal();
    String getToRoleDivCode();
    String getDivName();
}

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
public interface CurrentWorkflowDivisionProjection {
    Long getCurrentOwnerRole();
    Integer getCurrentStep();
    String getToRoleDivCode();
    String getDivName();
}
@Query(value = """
    SELECT c.div_name
    FROM gpf.workflow_transition b
    INNER JOIN div_mast c
        ON c.div_code = b.to_role_div_code
    WHERE b.workflow_id = :workflowId
      AND b.step_order = :stepOrder
    """, nativeQuery = true)
Optional<String> findDivisionNameByWorkflowAndStep(
    @Param("workflowId") Long workflowId,
    @Param("stepOrder") Integer stepOrder
);
}
