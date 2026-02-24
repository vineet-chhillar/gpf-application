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
}