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

@Override
public List<WorkflowTransitionViewDTO> getPreviousRoles(
        Long workflowId,
        Integer currentStep) {

    try {

        System.out.println("🔥 SERVICE HIT, workflowId: " + workflowId + ", currentStep: " + currentStep);

        List<WorkflowTransition> transitions =
                transitionRepo
                        .findByWorkflowIdAndStepOrderLessThanOrderByStepOrderDesc(
                                workflowId,
                                currentStep
                        );

        return transitions.stream()
                .map(t -> {

                    WorkflowTransitionViewDTO dto =
                            new WorkflowTransitionViewDTO();

                    dto.setRoleId(t.getFromRole());

                    String roleName = roleRepo.findById(t.getFromRole())
                            .map(r -> r.getRoleName())
                            .orElse("Role");

                    dto.setRoleName(roleName);

                    // 🔥 CRITICAL (FOR RETURN LOGIC)

                    dto.setStepOrder(t.getStepOrder());

                    return dto;

                })
                // 🔥 DISTINCT BY ROLE (IMPORTANT)
                .collect(java.util.stream.Collectors.toMap(
                        WorkflowTransitionViewDTO::getRoleId,
                        d -> d,
                        (a, b) -> a
                ))
                .values()
                .stream()
                .toList();

    } catch (Exception e) {
        e.printStackTrace();
        throw e;
    }
}

@Override
public WorkflowTransitionViewDTO getRoleByStep(Long workflowId, Integer step) {

    try {

        System.out.println("🔥 getRoleByStep HIT");
        System.out.println("workflowId: " + workflowId);
        System.out.println("step: " + step);

        WorkflowTransition t =
                transitionRepo
                        .findByWorkflowIdAndStepOrder(workflowId, step)
                        .orElseThrow(() ->
                                new RuntimeException("Step not found"));

        System.out.println("Transition: " + t);
        System.out.println("FromRole: " + t.getFromRole());

        WorkflowTransitionViewDTO dto = new WorkflowTransitionViewDTO();

        dto.setRoleId(t.getFromRole());

        String roleName = roleRepo.findById(t.getFromRole())
                .map(r -> r.getRoleName())
                .orElse("Role");

        dto.setRoleName(roleName);
        dto.setStepOrder(step);

        return dto;

    } catch (Exception e) {

        System.out.println("❌ ERROR IN getRoleByStep");
        e.printStackTrace();

        throw e;
    }
}
}