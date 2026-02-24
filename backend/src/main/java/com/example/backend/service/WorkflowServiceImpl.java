package com.example.backend.service;

import com.example.backend.dto.WorkflowTransitionViewDTO;
import com.example.backend.entity.WorkflowMaster;
import com.example.backend.entity.WorkflowTransition;
import com.example.backend.repository.WorkflowMasterRepository;
import com.example.backend.repository.WorkflowTransitionRepository;
import com.example.backend.repository.FunctionalRoleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkflowServiceImpl implements WorkflowService {

    @Autowired
    private WorkflowMasterRepository workflowMasterRepo;

    @Autowired
    private WorkflowTransitionRepository transitionRepo;

    @Autowired
    private FunctionalRoleRepository roleRepo;

    @Override
    public List<WorkflowMaster> getAllWorkflows() {
        return workflowMasterRepo.findAll();
    }

    @Override
    public List<WorkflowTransitionViewDTO> getWorkflowTransitions(Long workflowId) {

        List<WorkflowTransition> transitions =
                transitionRepo.findByWorkflowIdOrderByStepOrder(workflowId);

        return transitions.stream().map(t -> {

            WorkflowTransitionViewDTO dto = new WorkflowTransitionViewDTO();

            dto.setTransitionId(t.getTransitionId());
            dto.setStepOrder(t.getStepOrder());

            dto.setFromRole(
                    roleRepo.findById(t.getFromRole())
                            .map(r -> r.getRoleName())
                            .orElse("Unknown")
            );

            dto.setToRole(
                    roleRepo.findById(t.getToRole())
                            .map(r -> r.getRoleName())
                            .orElse("Unknown")
            );

            dto.setIsFinal(t.getIsFinal());

            return dto;

        }).toList();
    }
}