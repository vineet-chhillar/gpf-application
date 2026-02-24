package com.example.backend.repository;

import com.example.backend.entity.WorkflowMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowMasterRepository extends JpaRepository<WorkflowMaster, Long> {
}