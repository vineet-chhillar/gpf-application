package com.example.backend.service;

import com.example.backend.dto.ApplicationTrailDTO;
import com.example.backend.dto.GpfApplicationStatusResponseDTO;
import com.example.backend.dto.GpfWithdrawlRequestDTO;
import com.example.backend.dto.InboxApplicationDTO;
import com.example.backend.dto.WorkflowProcessRequestDTO;
import com.example.backend.entity.GpfWithdrawlDetails;
import com.example.backend.entity.GpfWithdrawlMaster;
import com.example.backend.entity.GpfWithdrawlRule;
import com.example.backend.entity.WorkflowTransition;
import com.example.backend.entity.ActionMaster;
import com.example.backend.entity.ApplicationStatusTrail;
import com.example.backend.repository.GpfWithdrawlDetailsRepository;
import com.example.backend.repository.GpfWithdrawlMasterRepository;
import com.example.backend.repository.GpfWithdrawlRuleRepository;
import com.example.backend.repository.WorkflowTransitionRepository;

import jakarta.persistence.Transient;

import com.example.backend.repository.ActionMasterRepository;
import com.example.backend.repository.ApplicationStatusTrailRepository;
import com.example.backend.repository.FunctionalRoleRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GpfWithdrawlServiceImpl implements GpfWithdrawlService {

    @Autowired
private FunctionalRoleRepository roleRepo;

    @Autowired
    private GpfWithdrawlMasterRepository masterRepo;

    @Autowired
    private GpfWithdrawlDetailsRepository detailsRepo;

    @Autowired
    private ActionMasterRepository actionRepo;

    @Autowired
    private WorkflowTransitionRepository workflowTransitionRepo;

    @Autowired
    private ApplicationStatusTrailRepository trailRepo;
@Autowired
private GpfWithdrawlRuleRepository withdrawlRuleRepo;
   

    @Override
public void saveWithdrawl(GpfWithdrawlRequestDTO dto) {

    if (dto == null || dto.getMaster() == null || dto.getDetails() == null) {
        throw new IllegalArgumentException("Invalid request payload");
    }

    GpfWithdrawlMaster master = dto.getMaster();
    GpfWithdrawlDetails details = dto.getDetails();

    System.out.println("Action ID: " + details.getAction().getActionId());
    System.out.println("Current Role: " + details.getCurrentOwnerRole());

    String empcode = master.getEmpcode();
    if (empcode == null || empcode.isBlank()) {
        throw new IllegalArgumentException("Empcode is mandatory");
    }

    if (masterRepo.findByEmpcode(empcode).isPresent()) {
        throw new IllegalStateException(
                "Withdrawal application already exists for this employee");
    }

    /* ================= BASIC VALIDATION ================= */

    if (details.getGpfaccountno() == null || details.getGpfaccountno().isBlank()) {
        throw new IllegalArgumentException("GPF Account No is mandatory");
    }

    if (details.getDateofapplication() == null) {
        throw new IllegalArgumentException("Date of application is mandatory");
    }

    /* ================= ACTION VALIDATION ================= */

    if (details.getAction() == null || details.getAction().getActionId() == null) {
        throw new IllegalArgumentException("Action is mandatory");
    }

    ActionMaster action = actionRepo.findById(details.getAction().getActionId())
            .orElseThrow(() -> new IllegalArgumentException("Invalid action id"));

    details.setAction(action);

    /* ================= WORKFLOW DETERMINATION ================= */

    String designation = master.getDesignation();
    Long workflowId = determineWorkflow(designation);
    details.setWorkflowId(workflowId);

    /* ================= FIRST TRANSITION ================= */

    Long employeeRoleId = 28L; // TODO: replace with dynamic role

    WorkflowTransition firstTransition =
            workflowTransitionRepo
                    .findFirstByWorkflowIdAndFromRoleOrderByStepOrder(
                            workflowId,
                            employeeRoleId
                    )
                    .orElseThrow(() ->
                            new IllegalStateException("Workflow not configured properly"));

    /* ================= RESUME / NORMAL FLOW ================= */

    {/*if (details.getIsReturned() != null && details.getIsReturned()) {

        System.out.println("🔥 RESUME FLOW (SAVE)");

        Integer resumeStep = details.getReturnFromStep();

        if (resumeStep == null) {
            throw new IllegalStateException("Return step missing");
        }

        // 🔥 MOVE FORWARD (NOT SAME STEP)
        WorkflowTransition next =
                workflowTransitionRepo
                        .findFirstByWorkflowIdAndStepOrderGreaterThanOrderByStepOrder(
                                workflowId,
                                resumeStep - 1
                        )
                        .orElseThrow(() ->
                                new IllegalStateException("Next workflow step missing"));

        details.setCurrentOwnerRole(next.getToRole());
        details.setCurrentStep(next.getStepOrder());

        // ✅ reset flags
        details.setIsReturned(false);
        details.setReturnFromStep(null);

    } else {*/}

        // ✅ NORMAL FIRST SUBMIT

        details.setCurrentOwnerRole(firstTransition.getToRole());
        details.setCurrentStep(firstTransition.getStepOrder());
    //}

    /* ================= SAVE MASTER & DETAILS ================= */

    master = masterRepo.save(master);

    details.setMaster(master);

    details = detailsRepo.save(details);

    /* ================= INSERT STATUS TRAIL ================= */

    ApplicationStatusTrail trail = new ApplicationStatusTrail();
    trail.setApplicationId(master.getId());
    trail.setActionId(action.getActionId());
    trail.setActionByRole(employeeRoleId);
    trail.setRemarks("Application Submitted");

    trailRepo.save(trail);
}

    private Long determineWorkflow(String designation) {

    if (designation == null || designation.isBlank()) {
        return 3L; // default workflow
    }

    String normalized = designation.trim().toUpperCase();

    // Workflow 2 → Scientist D to G
    if (normalized.equals("SCIENTIST-D") ||
        normalized.equals("SCIENTIST-E") ||
        normalized.equals("SCIENTIST-F") ||
        normalized.equals("SCIENTIST-G")) {

        return 2L;
    }

    // Workflow 1 → Scientist B, C and Section Officer
    if (normalized.equals("SCIENTIST-B") ||
        normalized.equals("SCIENTIST-C") ||
        normalized.equals("SECTION OFFICER")) {

        return 1L;
    }

    // Default → Workflow 3
    return 3L;
}


    @Override
public GpfWithdrawlRequestDTO getByEmpcode(String empcode) {

    if (empcode == null || empcode.isBlank()) {
        throw new IllegalArgumentException("Empcode is required");
    }

    GpfWithdrawlRequestDTO dto = new GpfWithdrawlRequestDTO();

    masterRepo.findByEmpcode(empcode)
            .ifPresent(dto::setMaster);

    detailsRepo.findByMaster_Empcode(empcode)
            .ifPresent(dto::setDetails);

    return dto;
}
@Override
@Transactional
public void processApplications(WorkflowProcessRequestDTO request) {

    if (request.getApplicationIds() == null || request.getApplicationIds().isEmpty()) {
        throw new IllegalArgumentException("No applications selected");
    }

    if (request.getActionId() == null) {
        throw new IllegalArgumentException("Action is mandatory");
    }

    ActionMaster action = actionRepo.findById(request.getActionId())
            .orElseThrow(() -> new IllegalArgumentException("Invalid action"));

    for (Long appId : request.getApplicationIds()) {

        GpfWithdrawlDetails details = detailsRepo.findByMaster_Id(appId)
                .orElseThrow(() ->
                        new IllegalStateException("Application not found: " + appId));

        if (details.getCurrentOwnerRole() == 0) {
            throw new IllegalStateException("Application already completed");
        }

        Long actingRole = details.getCurrentOwnerRole();
        Long workflowId = details.getWorkflowId();

        // 🔥 RESUME FLOW (HIGHEST PRIORITY)
        if (details.getIsReturned() != null && details.getIsReturned()) {

            System.out.println("🔥 RESUME FLOW IN PROCESS");

           Integer resumeStep = details.getReturnFromStep();

// 🔥 MOVE TO NEXT STEP DIRECTLY
WorkflowTransition next =
    workflowTransitionRepo
        .findFirstByWorkflowIdAndStepOrderGreaterThanOrderByStepOrder(
            workflowId,
            resumeStep - 2   // important
        )
        .orElseThrow(() -> new IllegalStateException("Next workflow step missing"));

details.setCurrentOwnerRole(next.getToRole());
details.setCurrentStep(next.getStepOrder());

            // ✅ RESET FLAGS
            details.setIsReturned(false);
            details.setReturnFromStep(null);

            details.setAction(action);

            detailsRepo.save(details);

            // ✅ TRAIL
            ApplicationStatusTrail trail = new ApplicationStatusTrail();
            trail.setApplicationId(details.getMaster().getId());
            trail.setActionId(action.getActionId());
            trail.setActionByRole(actingRole);
            trail.setRemarks(request.getRemarks());

            trailRepo.save(trail);

            continue; // 🔥 VERY IMPORTANT (skip normal flow)
        }

        WorkflowTransition transition =
                workflowTransitionRepo
                        .findFirstByWorkflowIdAndStepOrderOrderByStepOrder(
                                workflowId,
                                details.getCurrentStep()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException("Workflow step not found"));

        // 🔥 SEND BACK FLOW
        if (request.getSendToRole() != null) {

            System.out.println("🔥 SEND BACK FLOW");

            // ✅ store ORIGINAL step (corrected earlier)
            details.setReturnFromStep(details.getCurrentStep() + 1);
            details.setIsReturned(true);

            Long sendToRole = request.getSendToRole();

            WorkflowTransition target =
                    workflowTransitionRepo
                            .findFirstByWorkflowIdAndFromRoleOrderByStepOrder(
                                    workflowId,
                                    sendToRole
                            )
                            .orElseThrow(() ->
                                    new IllegalStateException("Invalid sendTo role"));

            details.setCurrentOwnerRole(sendToRole);
            details.setCurrentStep(target.getStepOrder());
            details.setAction(action);

        } else {

            // ✅ NORMAL FLOW

            if (transition.getIsFinal()) {

                details.setCurrentOwnerRole(0L);
                details.setAction(action);

            } else {

                WorkflowTransition next =
                        workflowTransitionRepo
                                .findFirstByWorkflowIdAndStepOrderGreaterThanOrderByStepOrder(
                                        workflowId,
                                        details.getCurrentStep()
                                )
                                .orElseThrow(() ->
                                        new IllegalStateException("Next workflow step missing"));

                details.setCurrentOwnerRole(next.getToRole());
                details.setCurrentStep(next.getStepOrder());
                details.setAction(action);
            }
        }

        detailsRepo.save(details);

        ApplicationStatusTrail trail = new ApplicationStatusTrail();
        trail.setApplicationId(details.getMaster().getId());
        trail.setActionId(action.getActionId());
        trail.setActionByRole(actingRole);
        trail.setRemarks(request.getRemarks());

        trailRepo.save(trail);
    }
}
@Override
public List<GpfWithdrawlDetails> getInboxByRole(Long roleId) {

    if (roleId == null) {
        throw new IllegalArgumentException("RoleId is required");
    }

    return detailsRepo.findByCurrentOwnerRole(roleId);
}
@Override
public GpfApplicationStatusResponseDTO getApplicationStatus(String empcode) {

    GpfWithdrawlMaster master = masterRepo.findByEmpcode(empcode)
            .orElseThrow(() -> new RuntimeException("Application not found"));

    GpfWithdrawlDetails details = detailsRepo
            .findByMaster_Empcode(empcode)
            .orElseThrow(() -> new RuntimeException("Details not found"));

    /* ===== CONVERT RULE ID → RULE NAME ===== */

 Long ruleId = details.getWithdrawlrule();

if (ruleId != null) {

    String ruleName = withdrawlRuleRepo
            .findById(ruleId)
            .map((GpfWithdrawlRule r) -> r.getWithdrawlReason())
            .orElse("Rule");

    details.setWithdrawlruleText(ruleName);
}

    /* ===== TRAIL ===== */

    List<ApplicationStatusTrail> trails =
            trailRepo.findByApplicationIdOrderByActionatAsc(master.getId());

    List<ApplicationTrailDTO> trailDTOs = trails.stream().map(t -> {

        ApplicationTrailDTO dto = new ApplicationTrailDTO();

        dto.setRole(resolveRoleName(t.getActionByRole()));
        dto.setAction(resolveActionName(t.getActionId()));
        dto.setRemarks(t.getRemarks());
        dto.setTime(t.getActionat().toString());

        return dto;

    }).toList();

    GpfApplicationStatusResponseDTO response = new GpfApplicationStatusResponseDTO();
    response.setMaster(master);
    response.setDetails(details);
    response.setTrail(trailDTOs);

    return response;
}
private String resolveRoleName(Long roleId) {

    if (roleId == null) return "";

    return roleRepo.findById(roleId)
            .map(r -> r.getRoleName())
            .orElse("Unknown Role");
}

private String resolveActionName(Long actionId) {

    if (actionId == null) return "";

    return actionRepo.findById(actionId)
            .map(ActionMaster::getActionDesc)
            .orElse("Action");
}
@Override
public List<ApplicationTrailDTO> getTrail(Long applicationId) {

    List<ApplicationStatusTrail> trails =
            trailRepo.findByApplicationIdOrderByActionatAsc(applicationId);

    return trails.stream().map(t -> {

        ApplicationTrailDTO dto = new ApplicationTrailDTO();

        String roleName = roleRepo.findById(t.getActionByRole())
                .map(r -> r.getRoleName())
                .orElse("Role");

        String actionName = actionRepo.findById(t.getActionId())
                .map(a -> a.getActionDesc())
                .orElse("Action");

        dto.setRole(roleName);
        dto.setAction(actionName);
        dto.setRemarks(t.getRemarks());

        if (t.getActionat() != null) {
            dto.setTime(t.getActionat().toString());
        }

        return dto;

    }).toList();
}


public List<GpfApplicationStatusResponseDTO> getAllApplicationStatus() {

    List<GpfWithdrawlMaster> masters = masterRepo.findAll();

    return masters.stream().map(master -> {

        GpfWithdrawlDetails details =
                detailsRepo.findByMaster_Empcode(master.getEmpcode()).orElse(null);

                if (details != null && details.getWithdrawlrule() != null) {

    withdrawlRuleRepo
        .findById(details.getWithdrawlrule())
        .ifPresent(rule ->
            details.setWithdrawlruleText(rule.getWithdrawlReason())
        );
}

        List<ApplicationStatusTrail> trails =
                trailRepo.findByApplicationIdOrderByActionatAsc(master.getId());

        List<ApplicationTrailDTO> trailDTO =
                trails.stream().map(t -> {

                    ApplicationTrailDTO dto = new ApplicationTrailDTO();

                    dto.setRole(resolveRoleName(t.getActionByRole()));
                    dto.setAction(resolveActionName(t.getActionId()));
                    dto.setRemarks(t.getRemarks());
                    dto.setTime(t.getActionat().toString());

                    return dto;

                }).toList();

        /* -------- GET LAST ACTION -------- */

        String lastRemarks = null;
        String lastActionByRole = null;

        if (!trails.isEmpty()) {

            ApplicationStatusTrail last =
                    trails.get(trails.size() - 1);

            lastRemarks = last.getRemarks();
            lastActionByRole = resolveRoleName(last.getActionByRole());
        }
String currentRoleName = "-";

if (details != null && details.getCurrentOwnerRole() != null) {

    if (details.getCurrentOwnerRole() == 0) {
        currentRoleName = "Completed";
    } else {
        currentRoleName = resolveRoleName(details.getCurrentOwnerRole());
    }
}
        /* -------- BUILD RESPONSE -------- */

        GpfApplicationStatusResponseDTO res = new GpfApplicationStatusResponseDTO();
        res.setMaster(master);
        res.setDetails(details);
        res.setTrail(trailDTO);

        res.setLastRemarks(lastRemarks);
        res.setLastActionByRole(lastActionByRole);
        res.setCurrentOwnerRole(currentRoleName);
        if (details != null) {
            res.setCurrentOwnerRole(resolveRoleName(details.getCurrentOwnerRole()));
        }

        return res;

    }).toList();
}
@Override
public Long getCurrentWorkflowRole() {

    return detailsRepo
            .findFirstByCurrentOwnerRoleNotOrderByCurrentOwnerRoleAsc(0L)
            .map(GpfWithdrawlDetails::getCurrentOwnerRole)
            .orElse(0L);
}


@Override
public List<InboxApplicationDTO> getAllPendingApplications() {

    List<GpfWithdrawlDetails> list =
            detailsRepo.findByCurrentOwnerRoleNot(0L);

    return list.stream().map(d -> {

        InboxApplicationDTO dto = new InboxApplicationDTO();

          dto.setApplicationId(d.getMaster().getId());  // FIX

        if (d.getMaster() != null) {
            dto.setEmployeeName(d.getMaster().getEmpname());
            dto.setEmpCode(d.getMaster().getEmpcode());
             dto.setDesignation(d.getMaster().getDesignation());
        }

       dto.setAmount(d.getAmountofwithdrawlrequested());
dto.setPurpose(d.getPurposeofwithdrawl());
dto.setCurrentOwnerRoleId(d.getCurrentOwnerRole());
        if (d.getDateofapplication() != null) {
            dto.setApplicationDate(d.getDateofapplication().toString());
        }

        String roleName = roleRepo.findById(d.getCurrentOwnerRole())
                .map(r -> r.getRoleName())
                .orElse("Role");

        dto.setPendingWithRole(roleName);

        return dto;

    }).toList();
}


}
