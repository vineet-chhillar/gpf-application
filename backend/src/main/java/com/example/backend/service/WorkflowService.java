package com.example.backend.service;

import com.example.backend.dto.WorkflowTransitionViewDTO;
import com.example.backend.entity.WorkflowMaster;

import java.util.List;

public interface WorkflowService {

    List<WorkflowMaster> getAllWorkflows();

    List<WorkflowTransitionViewDTO> getWorkflowTransitions(Long workflowId);

    List<WorkflowTransitionViewDTO> getPreviousRoles(Long workflowId, Integer currentStep);
    WorkflowTransitionViewDTO getRoleByStep(Long workflowId, Integer step);
}