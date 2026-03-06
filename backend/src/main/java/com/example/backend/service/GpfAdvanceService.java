package com.example.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.ApplicationTrailDTO;
import com.example.backend.dto.GpfApplicationStatusResponseDTO;
import com.example.backend.dto.InboxApplicationDTO;
import com.example.backend.dto.WorkflowProcessRequestDTO;
import com.example.backend.entity.ActionMaster;
import com.example.backend.entity.AdvanceApplicationStatusTrail;
import com.example.backend.entity.GpfAdvanceDetails;
import com.example.backend.entity.GpfAdvanceMaster;
import com.example.backend.entity.WorkflowTransition;
import com.example.backend.repository.AdvanceStatusTrailRepo;
import com.example.backend.repository.FunctionalRoleRepository;
import com.example.backend.repository.GpfAdvanceDetailsRepo;
import com.example.backend.repository.GpfAdvanceMasterRepo;
import com.example.backend.repository.GpfAdvanceRuleRepo;
import com.example.backend.repository.ActionMasterRepository;
import com.example.backend.repository.WorkflowTransitionRepository;

@Service
@Transactional
public class GpfAdvanceService {

    @Autowired
    private GpfAdvanceMasterRepo masterRepo;

    @Autowired
    private GpfAdvanceDetailsRepo detailsRepo;

    @Autowired
    private AdvanceStatusTrailRepo trailRepo;

    @Autowired
    private ActionMasterRepository actionRepo;

    @Autowired
    private GpfAdvanceRuleRepo advanceRuleRepo;

@Autowired
private FunctionalRoleRepository roleRepo;
    
    @Autowired
    private WorkflowTransitionRepository workflowTransitionRepo;

    
    /* ================= FINANCIAL YEAR METHODS ================= */

    private LocalDate getFinancialYearStart() {

        LocalDate today = LocalDate.now();

        int year = today.getYear();
        int month = today.getMonthValue();

        int fyStartYear = (month < 4) ? year - 1 : year;

        return LocalDate.of(fyStartYear, 4, 1);
    }

    private LocalDate getLastFinancialYearEnd() {

        LocalDate today = LocalDate.now();

        int year = today.getYear();
        int month = today.getMonthValue();

        int fyEndYear = (month < 4) ? year - 1 : year;

        return LocalDate.of(fyEndYear, 3, 31);
    }

    /* ================= WORKFLOW DETERMINATION ================= */

    private Long determineWorkflow(String designation) {

        if (designation == null || designation.isBlank()) {
            return 3L;
        }

        String d = designation.trim().toUpperCase();

        if (d.equals("SCIENTIST-D") ||
            d.equals("SCIENTIST-E") ||
            d.equals("SCIENTIST-F") ||
            d.equals("SCIENTIST-G")) {

            return 2L;
        }

        if (d.equals("SCIENTIST-B") ||
            d.equals("SCIENTIST-C") ||
            d.equals("SECTION OFFICER")) {

            return 1L;
        }

        return 3L;
    }

    /* ================= SAVE ADVANCE APPLICATION ================= */

    public void saveAdvanceApplication(
        GpfAdvanceMaster master,
        GpfAdvanceDetails details,
        Long roleId,
        Long actionId) {

    try {
System.out.println("===== SERVICE START =====");
        if (master == null || details == null) {
            throw new IllegalArgumentException("Invalid request payload");
        }

        if (roleId == null) {
            throw new IllegalArgumentException("RoleId is mandatory");
        }

        if (actionId == null) {
            throw new IllegalArgumentException("ActionId is mandatory");
        }

        String empcode = master.getEmpcode();

        if (empcode == null || empcode.isBlank()) {
            throw new IllegalArgumentException("Empcode is mandatory");
        }

        if (masterRepo.findByEmpcode(empcode).isPresent()) {
            throw new IllegalStateException(
                    "Advance application already exists for this employee");
        }

        if (details.getGpfaccountno() == null || details.getGpfaccountno().isBlank()) {
            throw new IllegalArgumentException("GPF Account No is mandatory");
        }

        if (details.getDateofapplication() == null) {
            throw new IllegalArgumentException("Date of application is mandatory");
        }

        Integer installments =
                details.getNoofmonthlyinstallmentsforpaymentofconsolidatedadvance();

        if (installments == null || installments <= 0) {
            throw new IllegalArgumentException("Installments must be provided");
        }

        if (installments > 60) {
            throw new IllegalArgumentException("Installments cannot exceed 60");
        }
System.out.println("Validation passed");
        ActionMaster action = actionRepo.findById(actionId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid action id"));

System.out.println("Action found: " + action.getActionDesc());
        master = masterRepo.save(master);

        details.setMaster(master);
        details.setCreatedat(LocalDateTime.now());

        details.setCreditfromdate(getFinancialYearStart());
        details.setCredittodate(LocalDate.now());

        details.setDateofoutstandingbalance(getLastFinancialYearEnd());

        details.setWithdrawlfromdate(getFinancialYearStart());
        details.setWithdrawltodate(LocalDate.now());

        details.setActionId(action.getActionId());

        Long workflowId = determineWorkflow(master.getDesignation());
        details.setWorkflowId(workflowId);

        WorkflowTransition transition =
                workflowTransitionRepo
                        .findFirstByWorkflowIdAndFromRoleOrderByStepOrder(
                                workflowId,
                                roleId
                        )
                        .orElseThrow(() ->
                                new IllegalStateException("Workflow not configured properly"));
                                System.out.println("Workflow ID: " + workflowId);
System.out.println("Transition found: " + transition.getToRole());
        details.setCurrentOwnerRole(transition.getToRole());
        details.setCurrentStep(transition.getStepOrder());

        detailsRepo.save(details);

        AdvanceApplicationStatusTrail trail =
                new AdvanceApplicationStatusTrail();

        trail.setApplicationid(master.getId());
        trail.setActionId(action.getActionId());
        trail.setActionByRole(roleId);
        trail.setRemarks("Application Submitted");
        trail.setActionat(LocalDateTime.now());

        trailRepo.save(trail);

    } catch (Exception e) {

        System.err.println("Error while saving advance application");
        e.printStackTrace();

        throw new RuntimeException("Failed to save advance application: " + e.getMessage());
    }
}
public List<InboxApplicationDTO> getAllPendingApplications() {

    List<GpfAdvanceDetails> list =
            detailsRepo.findByCurrentOwnerRoleNot(0L);
System.out.println("Advance inbox size = " + list.size());
    return list.stream().map(d -> {

        InboxApplicationDTO dto = new InboxApplicationDTO();

        if (d.getMaster() != null) {

            dto.setApplicationId(d.getMaster().getId());
            dto.setEmpCode(d.getMaster().getEmpcode());
            dto.setEmployeeName(d.getMaster().getEmpname());
            dto.setDesignation(d.getMaster().getDesignation());

        }

        dto.setAmount(d.getAmountofadvancerequested());
        dto.setPurpose(d.getPurposeofadvance());

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
public GpfApplicationStatusResponseDTO getApplicationStatus(String empcode) {

    GpfAdvanceMaster master =
            masterRepo.findByEmpcode(empcode)
                    .orElseThrow(() ->
                            new RuntimeException("Application not found"));

    GpfAdvanceDetails details =
            detailsRepo.findByMaster_Id(master.getId())
                    .orElseThrow(() ->
                            new RuntimeException("Details not found"));

                            Long ruleId = details.getAdvancerule();

    
System.out.println("Advance Rule ID: " + ruleId);

if(ruleId != null){

    String ruleName = advanceRuleRepo
            .findById(ruleId)
            .map(r -> r.getAdvanceReason())
            .orElse("Rule");

    System.out.println("Advance Rule Name: " + ruleName);

    details.setAdvanceruleText(ruleName);
}
    List<AdvanceApplicationStatusTrail> trails =
            trailRepo.findByApplicationidOrderByActionatAsc(master.getId());

    List<ApplicationTrailDTO> trailDTOs =
            trails.stream().map(t -> {

                ApplicationTrailDTO dto = new ApplicationTrailDTO();

                dto.setRole(String.valueOf(t.getActionByRole()));
                dto.setAction(String.valueOf(t.getActionId()));
                dto.setRemarks(t.getRemarks());

                if (t.getActionat() != null)
                    dto.setTime(t.getActionat().toString());

                return dto;

            }).toList();

    GpfApplicationStatusResponseDTO response =
            new GpfApplicationStatusResponseDTO();

    response.setMaster(master);
    response.setDetails(details);
    response.setTrail(trailDTOs);

    return response;
}
public List<ApplicationTrailDTO> getTrail(Long applicationId) {

    List<AdvanceApplicationStatusTrail> trails =
            trailRepo.findByApplicationidOrderByActionatAsc(applicationId);

    return trails.stream().map(t -> {

        ApplicationTrailDTO dto = new ApplicationTrailDTO();

        dto.setRole(String.valueOf(t.getActionByRole()));
        dto.setAction(String.valueOf(t.getActionId()));
        dto.setRemarks(t.getRemarks());

        if (t.getActionat() != null)
            dto.setTime(t.getActionat().toString());

        return dto;

    }).toList();
}
   public void processApplications(WorkflowProcessRequestDTO request) {

    try {

        if (request.getApplicationIds() == null || request.getApplicationIds().isEmpty()) {
            throw new IllegalArgumentException("No applications selected");
        }

        if (request.getActionId() == null) {
            throw new IllegalArgumentException("Action is mandatory");
        }

        ActionMaster action = actionRepo.findById(request.getActionId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid action"));

        for (Long appId : request.getApplicationIds()) {

            GpfAdvanceDetails details = detailsRepo.findByMaster_Id(appId)
                    .orElseThrow(() ->
                            new IllegalStateException("Application not found: " + appId));

            if (details.getCurrentOwnerRole() == 0) {
                throw new IllegalStateException("Application already completed");
            }

            Long actingRole = details.getCurrentOwnerRole();
            Long workflowId = details.getWorkflowId();

            WorkflowTransition transition =
                    workflowTransitionRepo
                            .findFirstByWorkflowIdAndStepOrderOrderByStepOrder(
                                    workflowId,
                                    details.getCurrentStep()
                            )
                            .orElseThrow(() ->
                                    new IllegalStateException("Workflow step not found"));

            if (transition.getIsFinal()) {

                details.setCurrentOwnerRole(0L);
                details.setActionId(action.getActionId());

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
                details.setActionId(action.getActionId());
            }

            detailsRepo.save(details);

            AdvanceApplicationStatusTrail trail =
                    new AdvanceApplicationStatusTrail();

            trail.setApplicationid(details.getMaster().getId());
            trail.setActionId(action.getActionId());
            trail.setActionByRole(actingRole);
            trail.setRemarks(request.getRemarks());
            trail.setActionat(LocalDateTime.now());

            trailRepo.save(trail);
        }

    } catch (Exception e) {

        System.err.println("Error while processing advance workflow");
        e.printStackTrace();

        throw new RuntimeException("Workflow processing failed: " + e.getMessage());
    }
}
public List<GpfApplicationStatusResponseDTO> getAllApplicationStatus() {

    List<GpfAdvanceMaster> masters = masterRepo.findAll();

    return masters.stream().map(master -> {

        GpfApplicationStatusResponseDTO response =
                new GpfApplicationStatusResponseDTO();

        response.setMaster(master);

        /* ===== DETAILS ===== */

        GpfAdvanceDetails details =
        detailsRepo.findByMaster_Id(master.getId()).orElse(null);

        if (details != null && details.getAdvancerule() != null) {

    advanceRuleRepo
        .findById(details.getAdvancerule())
        .ifPresent(rule ->
            details.setAdvanceruleText(rule.getAdvanceReason())
        );
}

if (details != null) {

    response.setDetails(details);

    if (details.getCurrentOwnerRole() != null && details.getCurrentOwnerRole() != 0) {
        response.setCurrentOwnerRole(
                resolveRoleName(details.getCurrentOwnerRole())
        );
    } else {
        response.setCurrentOwnerRole("Completed");
    }
}
        /* ===== TRAIL ===== */

        List<AdvanceApplicationStatusTrail> trails =
                trailRepo.findByApplicationidOrderByActionatAsc(master.getId());

if (!trails.isEmpty()) {

    AdvanceApplicationStatusTrail lastTrail =
            trails.get(trails.size() - 1);

    response.setLastActionByRole(
            resolveRoleName(lastTrail.getActionByRole())
    );

    response.setLastRemarks(lastTrail.getRemarks());
}

        List<ApplicationTrailDTO> trailDTOs = trails.stream().map(t -> {

            ApplicationTrailDTO dto = new ApplicationTrailDTO();

            dto.setRole(resolveRoleName(t.getActionByRole()));
            dto.setAction(resolveActionName(t.getActionId()));
            dto.setRemarks(t.getRemarks());

            if (t.getActionat() != null) {
                dto.setTime(t.getActionat().toString());
            }

            return dto;

        }).toList();

        response.setTrail(trailDTOs);

        return response;

    }).toList();
}
private String resolveRoleName(Long roleId) {

    if (roleId == null) return null;

    return roleRepo.findById(roleId)
            .map(r -> r.getRoleName())
            .orElse(String.valueOf(roleId));
}
private String resolveActionName(Long actionId) {

    if (actionId == null) return null;

    return actionRepo.findById(actionId)
            .map(a -> a.getActionDesc())
            .orElse(String.valueOf(actionId));
}
}