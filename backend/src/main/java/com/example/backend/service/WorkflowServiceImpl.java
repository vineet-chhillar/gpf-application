package com.example.backend.service;

import com.example.backend.dto.WorkflowMasterDTO;
import com.example.backend.dto.WorkflowTransitionViewDTO;
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
public List<WorkflowMasterDTO> getAllWorkflows() {

    List<WorkflowMasterRepository.WorkflowMasterProjection> results =   workflowMasterRepo.findWorkflowWithDivision();

    return results.stream().map(row -> {
        WorkflowMasterDTO dto = new WorkflowMasterDTO();

        dto.setWorkflowId(row.getWorkflowId());
        dto.setWorkflowName(row.getWorkflowName());
        dto.setDescription(row.getDescription());
        dto.setIsActive(row.getIsActive());
        dto.setCreatedAt(row.getCreatedAt());
        dto.setWorkflowDiv(row.getWorkflowDiv());
        dto.setDivName(row.getDivName());

        return dto;
    }).toList();
}


    
@Override
public List<WorkflowTransitionViewDTO> getWorkflowTransitions(Long workflowId) {

    List<WorkflowTransitionRepository.WorkflowTransitionProjection> transitions =
            transitionRepo.findWorkflowTransitionsWithDivision(workflowId);

    System.out.println("WorkflowId: " + workflowId);
    System.out.println("Transitions count: " + transitions.size());

    for (WorkflowTransitionRepository.WorkflowTransitionProjection tt : transitions) {
        System.out.println(
            "Step: " + tt.getStepOrder() +
            " From: " + tt.getFromRole() +
            " To: " + tt.getToRole() +
            " Final: " + tt.getIsFinal() +
            " Division Code: " + tt.getToRoleDivCode() +
            " Division Name: " + tt.getDivName()
        );
    }

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

        // New fields
        dto.setToRoleDivCode(t.getToRoleDivCode());
        dto.setDivName(t.getDivName());
System.out.print(dto.getToRoleDivCode());
System.out.print(dto.getDivName());
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