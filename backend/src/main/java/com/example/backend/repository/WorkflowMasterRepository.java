
package com.example.backend.repository;

import com.example.backend.entity.WorkflowMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.time.LocalDateTime;

public interface WorkflowMasterRepository
        extends JpaRepository<WorkflowMaster, Long> {

    @Query(value = """
        SELECT
            a.workflow_id AS "workflowId",
            a.workflow_name AS "workflowName",
            a.description AS "description",
            a.is_active AS "isActive",
            a.created_at AS "createdAt",
            a.div_code AS "workflowDiv",
            b.div_name AS "divName"
        FROM gpf.workflow_master a
        INNER JOIN public.div_mast b
            ON a.div_code = b.div_code
        """, nativeQuery = true)
    List<WorkflowMasterProjection> findWorkflowWithDivision();

    interface WorkflowMasterProjection {
        Long getWorkflowId();
        String getWorkflowName();
        String getDescription();
        Boolean getIsActive();
        LocalDateTime getCreatedAt();
        String getWorkflowDiv();
        String getDivName();
    }
}
