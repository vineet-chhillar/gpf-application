package com.example.backend.controller;

import com.example.backend.dto.WorkflowTransitionViewDTO;
import com.example.backend.entity.WorkflowMaster;
import com.example.backend.service.WorkflowService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflow")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    @GetMapping("/list")
    public List<WorkflowMaster> getAllWorkflows() {
        return workflowService.getAllWorkflows();
    }

    @GetMapping("/transitions/{workflowId}")
    public List<WorkflowTransitionViewDTO> getTransitions(
            @PathVariable Long workflowId) {

        return workflowService.getWorkflowTransitions(workflowId);
    }

   @GetMapping("/previous-roles/{workflowId}/{currentStep}")
public List<WorkflowTransitionViewDTO> getPreviousRoles(
        @PathVariable Long workflowId,
        @PathVariable Integer currentStep) {
 System.out.println("🔥 CONTROLLER HIT");
    return workflowService.getPreviousRoles(workflowId, currentStep);
}

@GetMapping("/role-by-step/{workflowId}/{step}")
public WorkflowTransitionViewDTO getRoleByStep(
        @PathVariable Long workflowId,
        @PathVariable Integer step) {

    return workflowService.getRoleByStep(workflowId, step);
}
    
}