package com.example.backend.service;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;

import com.example.backend.dto.ApiResponse;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.Transient;
import tools.jackson.databind.JsonNode;

import com.example.backend.repository.ActionMasterRepository;
import com.example.backend.repository.ApplicationStatusTrailRepository;
import com.example.backend.repository.FunctionalRoleRepository;
import com.example.backend.repository.GpfSanctionOrderRepository;
import com.example.backend.repository.GpfAdvanceDetailsRepo;
import com.example.backend.service.GpfEmployeeService;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
//import java.net.http.HttpHeaders;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.el.stream.Optional;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.Base64;
import java.util.Collections;
import jakarta.servlet.http.HttpSession;

import com.example.backend.dto.GpfEmployeeDetailsDTO;
import com.example.backend.dto.FunctionalRoleDTO;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class GpfWithdrawlServiceImpl implements GpfWithdrawlService {

@Autowired
private HttpSession session;
    private static final boolean USE_MOCK = false;

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
   
@Autowired
private GpfSanctionOrderRepository gpfSanctionOrderRepository;

@Autowired
private GpfAdvanceDetailsRepo advanceDetailsRepo;

private final GpfEmployeeService employeeService;

    public GpfWithdrawlServiceImpl(
            GpfEmployeeService employeeService) {
        this.employeeService = employeeService;
    }
private String getLoggedInEmpCode() {

    //Object empCode = session.getAttribute("empcode");
    Object employeeDetails =
            session.getAttribute("GPF_EMPLOYEE_DETAILS");

            if (!(employeeDetails instanceof GpfEmployeeDetailsDTO employee)) {
        throw new IllegalStateException(
                "Logged-in employee details not found in session"
        );
    }

            Long empCode=employee.getEmpCode();
System.out.println("getLoggedInEmpCode");
            System.out.println(employeeDetails);
            System.out.println(empCode);


    if (empCode == null || empCode.toString().isBlank()) {
        throw new IllegalStateException(
            "Logged-in employee code not found in session"
        );
    }

    return empCode.toString().trim();
}

private Set<String> getLoggedInRoleDivisionAssignments() {

    Object employeeDetails =
            session.getAttribute("GPF_EMPLOYEE_DETAILS");

    if (!(employeeDetails instanceof GpfEmployeeDetailsDTO employee)) {
        throw new IllegalStateException(
                "Logged-in employee details not found in session"
        );
    }

    System.out.println("========== SESSION ASSIGNMENT DEBUG ==========");
    System.out.println("Employee Code: " + employee.getEmpCode());

    if (employee.getFunctionalRoles() == null
            || employee.getFunctionalRoles().isEmpty()) {
        throw new IllegalStateException(
                "No functional roles found for logged-in employee"
        );
    }

    Set<String> assignments = new HashSet<>();

    employee.getFunctionalRoles().forEach(role -> {

        System.out.println(
                "Session Role: postId=" + role.getPostId()
                + ", divCode=[" + role.getDivCode() + "]"
        );

        if (role.getPostId() != null
                && role.getDivCode() != null
                && !role.getDivCode().isBlank()) {

            String assignment =
                    role.getPostId() + "|" + role.getDivCode().trim();

            assignments.add(assignment);

            System.out.println("Generated Assignment: " + assignment);
        }
    });

    System.out.println("Final Assignment Set: " + assignments);
    System.out.println("==============================================");

    return assignments;
}


{/*private String getLoggedInEmpCode() {
    Object empCode = session.getAttribute("empcode");

    //Object empCode="6507";
    if (empCode == null || empCode.toString().isBlank()) {
        throw new IllegalStateException(
            "Logged-in employee code not found in session"
        );
    }

    return empCode.toString();
}*/}
@Override
public ApiResponse<?> saveWithdrawl(GpfWithdrawlRequestDTO dto) {

    if (dto == null || dto.getMaster() == null || dto.getDetails() == null) {
        return ApiResponse.failure("Invalid request payload");
    }

    GpfWithdrawlMaster master = dto.getMaster();
    GpfWithdrawlDetails details = dto.getDetails();

    System.out.println("Prior Withdrawal Financial Year: " + details.getPriorwithdrawlfinyear());
    System.out.println("FULL DTO: " + dto);
System.out.println("MASTER OBJECT: " + dto.getMaster());
System.out.println("EMPCODE RAW: " + (dto.getMaster() != null ? dto.getMaster().getEmpcode() : "MASTER NULL"));

    System.out.println("Action ID: " + details.getAction().getActionId());
    System.out.println("Current Role: " + details.getCurrentOwnerRole());

    String empcode = master.getEmpcode();
    System.out.println("emp code: " + empcode);
    if (empcode == null || empcode.isBlank()) {
        return ApiResponse.failure("Empcode is mandatory");
    }

   /* ================= CHECK ACTIVE APPLICATIONS ================= */

// Check active WITHDRAWAL
// 🔹 Check active WITHDRAWAL
boolean activeWithdrawal =
        detailsRepo.existsActiveWithdrawal(empcode);

if (activeWithdrawal) {
    return ApiResponse.failure(
        "A withdrawal application is already under process for this employee"
    );
}


// 🔹 Check active ADVANCE
boolean activeAdvance =
        advanceDetailsRepo.existsActiveAdvance(empcode);

if (activeAdvance) {
    return ApiResponse.failure(
        "An advance application is already under process for this employee. " +
        "A withdrawal application cannot be submitted until it is completed or cancelled."
    );
}

    /* ================= BASIC VALIDATION ================= */

    if (details.getGpfaccountno() == null || details.getGpfaccountno().isBlank()) {
        return ApiResponse.failure("GPF Account No is mandatory");
    }

    if (details.getDateofapplication() == null) {
        return ApiResponse.failure("Date of application is mandatory");
    }

    /* ================= ACTION VALIDATION ================= */

    if (details.getAction() == null || details.getAction().getActionId() == null) {
        return ApiResponse.failure("Action is mandatory");
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
                .orElse(null);

if (firstTransition == null) {
    return ApiResponse.failure("Workflow not configured properly");
}

    /* ================= RESUME / NORMAL FLOW ================= */

    
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
    return ApiResponse.success(
    "Withdrawal application submitted successfully",
    master.getId() // optional: return application id
);
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

    // ✅ Step 1: get latest master
    List<GpfWithdrawlMaster> masters =
            masterRepo.findByEmpcodeOrderByIdDesc(empcode);

    if (masters.isEmpty()) {
        return dto; // or throw exception if required
    }

    GpfWithdrawlMaster master = masters.get(0);
    dto.setMaster(master);

    // ✅ Step 2: get details by master ID (NOT empcode)
    detailsRepo.findByMaster_Id(master.getId())
            .ifPresent(dto::setDetails);

    return dto;
}
@Override
@Transactional
public void processApplications(WorkflowProcessRequestDTO request) {

    String stage = "Starting processApplications";

    try {
        System.out.println("========== PROCESS APPLICATIONS START ==========");

        if (request.getApplicationIds() == null
                || request.getApplicationIds().isEmpty()) {
            throw new IllegalArgumentException("No applications selected");
        }

        if (request.getActionId() == null) {
            throw new IllegalArgumentException("Action is mandatory");
        }

        stage = "Fetching action";
        ActionMaster action = actionRepo.findById(request.getActionId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid action"));

        for (Long appId : request.getApplicationIds()) {

            stage = "Processing application ID: " + appId;

            try {
                System.out.println("\n========== APPLICATION PROCESSING ==========");
                System.out.println("Application ID: " + appId);
                System.out.println("Action ID: " + action.getActionId());

                stage = "Fetching application details";
                GpfWithdrawlDetails details = detailsRepo.findByMaster_Id(appId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Application not found: " + appId));

                if (details.getCurrentOwnerRole() == null) {
                    throw new IllegalStateException(
                            "Current owner role is null for application: " + appId);
                }

                if (details.getCurrentOwnerRole() == 0L) {
                    throw new IllegalStateException(
                            "Application already completed: " + appId);
                }

                Long actingRole = details.getCurrentOwnerRole();
                Long workflowId = details.getWorkflowId();

                System.out.println("Acting role: " + actingRole);
                System.out.println("Workflow ID: " + workflowId);
                System.out.println("Current step: " + details.getCurrentStep());

                stage = "Fetching logged-in employee code";
                String actionByEmpCode = getLoggedInEmpCode();
                System.out.println("Employee code: " + actionByEmpCode);

                Integer actingStep = details.getCurrentStep();

                if (workflowId == null || actingStep == null) {
                    throw new IllegalStateException(
                            "Workflow ID or current step is null. Workflow ID: "
                                    + workflowId + ", Step: " + actingStep);
                }

                stage = "Fetching acting workflow transition";
                WorkflowTransition actingTransition =
                        workflowTransitionRepo
                                .findFirstByWorkflowIdAndStepOrderOrderByStepOrder(
                                        workflowId, actingStep)
                                .orElseThrow(() ->
                                        new IllegalStateException(
                                                "Workflow transition not found. Workflow ID: "
                                                        + workflowId
                                                        + ", Step: " + actingStep));

                String actionByDivCode = actingTransition.getToRoleDivCode();

                System.out.println("Acting transition found successfully");
                System.out.println("Action by division: " + actionByDivCode);

                // RESUME FLOW
                if (Boolean.TRUE.equals(details.getIsReturned())) {

                    System.out.println("RESUME FLOW IN PROCESS");

                    stage = "Fetching next transition for resume flow";

                    Integer resumeStep = details.getReturnFromStep();

                    if (resumeStep == null) {
                        throw new IllegalStateException(
                                "Return-from-step is null for application: " + appId);
                    }

                    WorkflowTransition next =
                            workflowTransitionRepo
                                    .findFirstByWorkflowIdAndStepOrderGreaterThanOrderByStepOrder(
                                            workflowId, resumeStep - 2)
                                    .orElseThrow(() ->
                                            new IllegalStateException(
                                                    "Next workflow step missing during resume. "
                                                            + "Workflow ID: " + workflowId
                                                            + ", Resume step: " + resumeStep));

                    details.setCurrentOwnerRole(next.getToRole());
                    details.setCurrentStep(next.getStepOrder());
                    details.setIsReturned(false);
                    details.setReturnFromStep(null);
                    details.setAction(action);

                    stage = "Saving resumed application details";
                    detailsRepo.save(details);

                    stage = "Saving resume status trail";
                    ApplicationStatusTrail trail = new ApplicationStatusTrail();
                    trail.setApplicationId(details.getMaster().getId());
                    trail.setActionId(action.getActionId());
                    trail.setActionByRole(actingRole);
                    trail.setRemarks(request.getRemarks());
                    trail.setActionByDivCode(actionByDivCode);
                    trail.setActionByEmpCode(actionByEmpCode);

                    trailRepo.save(trail);

                    System.out.println("Resume flow completed for application: " + appId);
                    continue;
                }

                stage = "Fetching current workflow transition";
                WorkflowTransition transition =
                        workflowTransitionRepo
                                .findFirstByWorkflowIdAndStepOrderOrderByStepOrder(
                                        workflowId, details.getCurrentStep())
                                .orElseThrow(() ->
                                        new IllegalStateException(
                                                "Workflow step not found. Workflow ID: "
                                                        + workflowId
                                                        + ", Step: " + details.getCurrentStep()));

                // CANCEL / REJECT FLOW
                if (action.getActionId() == 12L) {

                    System.out.println("CANCEL / REJECT FLOW IN PROCESS");

                    details.setCurrentOwnerRole(0L);
                    details.setCurrentStep(0);
                    details.setAction(action);

                    stage = "Saving cancelled/rejected application";
                    detailsRepo.save(details);

                    stage = "Saving cancel/reject status trail";
                    ApplicationStatusTrail trail = new ApplicationStatusTrail();
                    trail.setApplicationId(details.getMaster().getId());
                    trail.setActionId(action.getActionId());
                    trail.setActionByRole(actingRole);
                    trail.setRemarks(request.getRemarks());
                    trail.setActionByDivCode(actionByDivCode);
                    trail.setActionByEmpCode(actionByEmpCode);

                    trailRepo.save(trail);

                    System.out.println("Cancel/reject completed for application: " + appId);
                    continue;
                }

                // SEND BACK FLOW
                if (request.getSendToRole() != null) {

                    System.out.println("SEND BACK FLOW");

                    details.setReturnFromStep(details.getCurrentStep() + 1);
                    details.setIsReturned(true);

                    Long sendToRole = request.getSendToRole();

                    stage = "Fetching send-back target transition";
                    WorkflowTransition target =
                            workflowTransitionRepo
                                    .findFirstByWorkflowIdAndFromRoleOrderByStepOrder(
                                            workflowId, sendToRole)
                                    .orElseThrow(() ->
                                            new IllegalStateException(
                                                    "Invalid send-to role: " + sendToRole
                                                            + ", Workflow ID: " + workflowId));

                    details.setCurrentOwnerRole(sendToRole);
                    details.setCurrentStep(target.getStepOrder());
                    details.setAction(action);

                } else {

                    // NORMAL FLOW
                    if (Boolean.TRUE.equals(transition.getIsFinal())) {

                        System.out.println("Final workflow step reached");

                        details.setCurrentOwnerRole(0L);
                        details.setCurrentStep(0);
                        details.setAction(action);

                    } else {

                        stage = "Fetching next workflow transition";

                        System.out.println("Fetching next transition. Workflow ID: "
                                + workflowId + ", Current step: "
                                + details.getCurrentStep());

                        WorkflowTransition next =
                                workflowTransitionRepo
                                        .findFirstByWorkflowIdAndStepOrderGreaterThanOrderByStepOrder(
                                                workflowId, details.getCurrentStep())
                                        .orElseThrow(() ->
                                                new IllegalStateException(
                                                        "Next workflow step missing. Workflow ID: "
                                                                + workflowId
                                                                + ", Current step: "
                                                                + details.getCurrentStep()));

                        System.out.println("Next transition found. Step: "
                                + next.getStepOrder()
                                + ", Role: " + next.getToRole());

                        details.setCurrentOwnerRole(next.getToRole());
                        details.setCurrentStep(next.getStepOrder());
                        details.setAction(action);
                    }
                }

                stage = "Saving application details";
                detailsRepo.save(details);
                System.out.println("Application details saved");

                stage = "Saving application status trail";
                ApplicationStatusTrail trail = new ApplicationStatusTrail();
                trail.setApplicationId(details.getMaster().getId());
                trail.setActionId(action.getActionId());
                trail.setActionByRole(actingRole);
                trail.setRemarks(request.getRemarks());
                trail.setActionByDivCode(actionByDivCode);
                trail.setActionByEmpCode(actionByEmpCode);

                trailRepo.save(trail);

                System.out.println("Status trail saved successfully");
                System.out.println("Application processed successfully: " + appId);

            } catch (Exception e) {

                System.err.println("\n========== APPLICATION PROCESSING ERROR ==========");
                System.err.println("Application ID: " + appId);
                System.err.println("Stage: " + stage);
                System.err.println("Exception: " + e.getClass().getName());
                System.err.println("Message: " + e.getMessage());
                e.printStackTrace();
                System.err.println("==================================================");

                // Re-throw so Spring rolls back the transaction.
                throw new IllegalStateException(
                        "Failed to process application " + appId
                                + " at stage: " + stage,
                        e);
            }
        }

        System.out.println("========== PROCESS APPLICATIONS COMPLETED ==========");

    } catch (Exception e) {

        System.err.println("\n========== PROCESS APPLICATIONS FAILED ==========");
        System.err.println("Last stage: " + stage);
        System.err.println("Exception: " + e.getClass().getName());
        System.err.println("Message: " + e.getMessage());
        e.printStackTrace();
        System.err.println("=================================================");

        throw e;
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

    List<GpfWithdrawlMaster> masters =
            masterRepo.findByEmpcodeOrderByIdDesc(empcode);

    if (masters.isEmpty()) {
        throw new RuntimeException("Application not found");
    }

    GpfWithdrawlMaster master = masters.get(0); // latest

    GpfWithdrawlDetails details = detailsRepo
            .findByMaster_Id(master.getId())
            .orElseThrow(() -> new RuntimeException("Details not found"));

    Long ruleId = details.getWithdrawlrule();

    if (ruleId != null) {
        String ruleName = withdrawlRuleRepo
                .findById(ruleId)
                .map(r -> r.getWithdrawlReason())
                .orElse("Rule");

        details.setWithdrawlruleText(ruleName);
    }

    List<ApplicationStatusTrail> trails =
            trailRepo.findByApplicationIdOrderByActionatAsc(master.getId());

    List<ApplicationTrailDTO> trailDTOs = trails.stream().map(t -> {

        ApplicationTrailDTO dto = new ApplicationTrailDTO();

        dto.setRole(resolveRoleName(t.getActionByRole()));
        dto.setAction(resolveActionName(t.getActionId()));
        dto.setRemarks(t.getRemarks());
        dto.setTime(
            t.getActionat() != null ? t.getActionat().toString() : ""
        );

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

public List<GpfApplicationStatusResponseDTO> getApplicationStatusByEmp(String empcode) {

    // 🔥 FETCH ONLY EMPLOYEE DATA
    List<GpfWithdrawlMaster> masters =
            masterRepo.findByEmpcodeOrderByIdDesc(empcode);

    return masters.stream().map(master -> {
        try {

            // 🔹 DETAILS (latest)
            List<GpfWithdrawlDetails> detailsList =
                    detailsRepo.findByMaster_EmpcodeOrderByIdDesc(master.getEmpcode());

            GpfWithdrawlDetails details =
                    detailsList.isEmpty() ? null : detailsList.get(0);

            // 🔹 RULE TEXT
            if (details != null && details.getWithdrawlrule() != null)
                 {
                withdrawlRuleRepo.findById(details.getWithdrawlrule())
                .ifPresent(rule -> details.setWithdrawlruleText(rule.getWithdrawlReason()));
            }

            // 🔹 TRAIL
            List<ApplicationStatusTrail> trails = trailRepo.findByApplicationIdOrderByActionatAsc(master.getId());

            List<ApplicationTrailDTO> trailDTO =  trails.stream().map(t -> {

                        ApplicationTrailDTO dto = new ApplicationTrailDTO();

                        dto.setRole(t.getActionByRole() != null  ? resolveRoleName(t.getActionByRole())  : "-");

                        dto.setAction(resolveActionName(t.getActionId()));
                        dto.setRemarks(t.getRemarks());

                        dto.setTime(
                                t.getActionat() != null
                                        ? t.getActionat().toString()
                                        : ""
                        );

                        return dto;

                    }).toList();

            // 🔹 LAST ACTION
            String lastRemarks = null;
            String lastActionByRole = null;

            if (!trails.isEmpty()) {
                ApplicationStatusTrail last = trails.get(trails.size() - 1);
                lastRemarks = last.getRemarks();
                lastActionByRole = resolveRoleName(last.getActionByRole());
            }

            // 🔹 CURRENT STATUS
            String currentRoleName = "-";

            if (details != null && details.getCurrentOwnerRole() != null) {

                Long lastActionId = null;

                if (!trails.isEmpty()) {
                    ApplicationStatusTrail last = trails.get(trails.size() - 1);
                    lastActionId = last.getActionId();
                }

                if (details.getCurrentOwnerRole() == 0) {

                    if (lastActionId != null && lastActionId == 12L) {
                        currentRoleName = "Cancelled/Rejected";
                    } else {
                        currentRoleName = "Completed";
                    }

                } else {
                    currentRoleName = resolveRoleName(details.getCurrentOwnerRole());
                }
            }

            // 🔹 BUILD RESPONSE
         // 🔹 BUILD RESPONSE
GpfApplicationStatusResponseDTO res =
        new GpfApplicationStatusResponseDTO();

res.setMaster(master);
res.setDetails(details);
res.setTrail(trailDTO);

res.setLastRemarks(lastRemarks);
res.setLastActionByRole(lastActionByRole);
res.setCurrentOwnerRole(currentRoleName);

// 🔹 CURRENT DIVISION NAME
String currentDivisionName = "-";

if (details != null
        && details.getWorkflowId() != null
        && details.getCurrentStep() != null
        && details.getCurrentStep() > 0) {

    currentDivisionName =
            workflowTransitionRepo.findDivisionNameByWorkflowAndStep(
                    details.getWorkflowId(),
                    details.getCurrentStep()
            ).orElse("-");
}

res.setCurrentDivisionName(currentDivisionName);
System.out.println("sdfgdgffdgdfhdfhdfghdfghdfhdfghg" + currentDivisionName);
return res;

        } catch (Exception e) {
            System.out.println("❌ Error for master ID: " + master.getId());
            e.printStackTrace();
            throw e;
        }

    }).toList();
}
public List<GpfApplicationStatusResponseDTO> getAllApplicationStatus() {

    List<GpfWithdrawlMaster> masters = masterRepo.findAll();

    return masters.stream().map(master -> {
try {
        
                List<GpfWithdrawlDetails> detailsList =
    detailsRepo.findByMaster_EmpcodeOrderByIdDesc(master.getEmpcode());

GpfWithdrawlDetails details =
    detailsList.isEmpty() ? null : detailsList.get(0); // latest

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

                    dto.setRole(
    t.getActionByRole() != null
        ? resolveRoleName(t.getActionByRole())
        : "-"
);
                    dto.setAction(resolveActionName(t.getActionId()));
                    dto.setRemarks(t.getRemarks());
                    dto.setTime(
    t.getActionat() != null 
        ? t.getActionat().toString() 
        : ""
);

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

    // 🔥 CHECK LAST ACTION
    Long lastActionId = null;

    if (!trails.isEmpty()) {
        ApplicationStatusTrail last = trails.get(trails.size() - 1);
        lastActionId = last.getActionId();
    }

    if (details.getCurrentOwnerRole() == 0) {

        if (lastActionId != null && lastActionId == 12L) {
            currentRoleName = "Cancelled/Rejected";   // ✅ NEW
        } else {
            currentRoleName = "Completed";
        }

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

        String currentDivisionName = "-";

if (details != null
        && details.getWorkflowId() != null
        && details.getCurrentStep() != null
        && details.getCurrentStep() > 0) {

    currentDivisionName =
        workflowTransitionRepo.findDivisionNameByWorkflowAndStep(
            details.getWorkflowId(),
            details.getCurrentStep()
        ).orElse("-");
}

res.setCurrentDivisionName(currentDivisionName);


        boolean sanctionExists =
        gpfSanctionOrderRepository.existsByApplicationId(master.getId());

res.setSanctionGenerated(sanctionExists);
        {/*if (details != null) {
            res.setCurrentOwnerRole(resolveRoleName(details.getCurrentOwnerRole()));
        }*/}

        return res;
} catch (Exception e) {

        System.out.println("❌ Error for master ID: " + master.getId());
        e.printStackTrace();

        throw e; // rethrow so you still see 500
    }

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

    
    // 1. Get assignments from the logged-in employee
    Set<String> assignments =
            new HashSet<>(getLoggedInRoleDivisionAssignments());

    System.out.println("========== INITIAL ASSIGNMENTS ==========");
    System.out.println("Logged-in employee assignments:");
    assignments.forEach(a -> System.out.println("  " + a));
    System.out.println("==========================================");

    // 2. Read logged-in employee details from session
    Object employeeDetails =
            session.getAttribute("GPF_EMPLOYEE_DETAILS");

    System.out.println("Session employee details type: "
            + (employeeDetails == null
                    ? "NULL"
                    : employeeDetails.getClass().getName()));

    if (employeeDetails instanceof GpfEmployeeDetailsDTO employee) {

        System.out.println("Logged-in employee code: "
                + employee.getEmpCode());

        if (employee.getFunctionalRoles() != null) {
            System.out.println("Logged-in employee functional roles:");

            employee.getFunctionalRoles().forEach(role ->
                System.out.println(
                    "  postId=" + role.getPostId()
                    + ", divCode=[" + role.getDivCode() + "]"
                )
            );
        }

        boolean isLinkOfficer =
                employee.getFunctionalRoles() != null
                && employee.getFunctionalRoles().stream()
                    .anyMatch(role ->
                        role.getPostId() != null
                        && role.getPostId().longValue() == 13L
                    );

        System.out.println("Is Link Officer: " + isLinkOfficer);

        // 3. Add linked employee assignments only for Link Officers
        if (isLinkOfficer) {

            Long empCode = employee.getEmpCode();

            employeeService.getLinkedEmployeeCode(empCode)
                .flatMap(employeeService::getEmployeeDetails)
                .ifPresentOrElse(linkedEmployee -> {

                    System.out.println(
                        "Linked employee assignments being added:"
                    );

                    if (linkedEmployee.getFunctionalRoles() != null) {

                        linkedEmployee.getFunctionalRoles().stream()
                            .filter(role -> role.getPostId() != null)
                            .filter(role -> role.getDivCode() != null
                                    && !role.getDivCode().isBlank())
                            .map(role -> role.getPostId()
                                    + "|" + role.getDivCode().trim())
                            .forEach(a -> {
                                System.out.println(
                                    "  Linked assignment: " + a
                                );
                                assignments.add(a);
                            });
                    }

                }, () -> System.out.println(
                    "No linked employee details found."
                ));
        }
    } else {
        System.out.println(
            "WARNING: Session employee details are missing or have an unexpected type."
        );
    }

    System.out.println("========== FINAL ASSIGNMENTS ==========");
    assignments.forEach(a -> System.out.println("  " + a));
    System.out.println("=======================================");


    // 4. Fetch pending withdrawal applications
    List<GpfWithdrawlDetails> list =
            detailsRepo.findByCurrentOwnerRoleNot(0L);

    // 5. Apply the existing workflow filters and return the combined results
    return list.stream()

            .filter(d -> d.getCurrentOwnerRole() != null
                    && d.getWorkflowId() != null
                    && d.getCurrentStep() != null
                    && d.getCurrentStep() > 0)

            
.filter(d -> {
    java.util.Optional<WorkflowTransition> transitionOpt =
            workflowTransitionRepo
                    .findFirstByWorkflowIdAndStepOrderOrderByStepOrder(
                            d.getWorkflowId(),
                            d.getCurrentStep()
                    );

    String divisionCode = transitionOpt
            .map(WorkflowTransition::getToRoleDivCode)
            .orElse(null);

    System.out.println("========== WITHDRAWAL INBOX DEBUG ==========");
    System.out.println("Application ID : "
            + (d.getMaster() != null ? d.getMaster().getId() : null));
    System.out.println("Workflow ID    : " + d.getWorkflowId());
    System.out.println("Current Step   : " + d.getCurrentStep());
    System.out.println("Current Owner   : " + d.getCurrentOwnerRole());
    System.out.println("To Role Div Code: [" + divisionCode + "]");

    if (divisionCode == null || divisionCode.isBlank()) {
        System.out.println("RESULT: EXCLUDED - Division code is missing");
        return false;
    }

    String assignment =
            d.getCurrentOwnerRole() + "|" + divisionCode.trim();

    boolean matches = assignments.contains(assignment);

    System.out.println("Assignment     : [" + assignment + "]");
    System.out.println("All Assignments: " + assignments);
    System.out.println("Assignment Match: " + matches);
    System.out.println("============================================");

    return matches;
})


            .map(d -> {
                InboxApplicationDTO dto = new InboxApplicationDTO();

                if (d.getMaster() != null) {
                    dto.setApplicationId(d.getMaster().getId());
                    dto.setEmployeeName(d.getMaster().getEmpname());
                    dto.setEmpCode(d.getMaster().getEmpcode());
                    dto.setDesignation(d.getMaster().getDesignation());
                }

                dto.setAmount(d.getAmountofwithdrawlrequested());
                dto.setPurpose(d.getPurposeofwithdrawl());
                dto.setCurrentOwnerRoleId(d.getCurrentOwnerRole());

                if (d.getDateofapplication() != null) {
                    dto.setApplicationDate(
                            d.getDateofapplication().toString()
                    );
                }

                String roleName = roleRepo
                        .findById(d.getCurrentOwnerRole())
                        .map(r -> r.getRoleName())
                        .orElse("Role");

                dto.setPendingWithRole(roleName);

                String divisionName =
                        workflowTransitionRepo
                                .findDivisionNameByWorkflowAndStep(
                                        d.getWorkflowId(),
                                        d.getCurrentStep()
                                )
                                .orElse("-");

                dto.setCurrentDivisionName(divisionName);

                return dto;
            })
            .toList();
}

private boolean isLinkOfficer() {

    Object employeeDetails =
            session.getAttribute("GPF_EMPLOYEE_DETAILS");

    if (!(employeeDetails instanceof GpfEmployeeDetailsDTO employee)
            || employee.getFunctionalRoles() == null) {
        return false;
    }

    return employee.getFunctionalRoles().stream()
            .anyMatch(role ->
                    role.getPostId() != null
                    && role.getPostId() == 13L
            );
}


private void validateWithdrawal(GpfWithdrawlDetails d) {

    /* ================= BASIC ================= */

    if (d.getAmountofwithdrawlrequested() == null ||
        d.getAmountofwithdrawlrequested().compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("Withdrawal amount must be greater than 0");
    }

    if (d.getPurposeofwithdrawl() == null ||
        d.getPurposeofwithdrawl().isBlank()) {
        throw new IllegalArgumentException("Purpose of withdrawal is required");
    }

    if (d.getWithdrawlrule() == null) {
        throw new IllegalArgumentException("Withdrawal rule is required");
    }

    if (d.getConcernedofficername() == null ||
        d.getConcernedofficername().isBlank()) {
        throw new IllegalArgumentException("Concerned officer name is required");
    }

    /* ================= PRIOR WITHDRAWAL ================= */

    if (Boolean.TRUE.equals(d.getIspriorwithdrawlforsamepurpose())) {

        if (d.getPriorwithdrawlamount() == null ||
            d.getPriorwithdrawlamount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "Prior withdrawal amount must be provided");
        }

        if (d.getPriorwithdrawlfinyear() == null ||
            d.getPriorwithdrawlfinyear().isBlank()) {
            throw new IllegalArgumentException(
                "Prior withdrawal financial year is required");
        }

    }

    /* ================= BUSINESS RULE ================= */

    // Example: amount should not exceed some limit (optional)
    //if (d.getAmountofwithdrawlrequested() > 1000000) {
       // throw new IllegalArgumentException("Withdrawal amount exceeds allowed limit");
   // }
}
@Override
@Transactional
public void updateWithdrawal(Long id, GpfWithdrawlRequestDTO dto) {

    GpfWithdrawlDetails entity = detailsRepo.findByMaster_Id(id)
            .orElseThrow(() ->
                    new IllegalStateException("Application not found: " + id));

    // 🔥 ALLOW EDIT ONLY IN RETURN MODE
    if (!Boolean.TRUE.equals(entity.getIsReturned())) {
        throw new IllegalStateException("Editing allowed only for returned applications");
    }

    GpfWithdrawlDetails payload = dto.getDetails();

    validateWithdrawal(payload);

    if (payload == null) {
        throw new IllegalArgumentException("Details payload is required");
    }
if (payload.getAmountofwithdrawlrequested()
        .compareTo(entity.getNetbalance()) > 0) {

    throw new IllegalArgumentException("Requested amount exceeds available balance");
}
    /* ================= UPDATE EDITABLE FIELDS ================= */

    entity.setAmountofwithdrawlrequested(payload.getAmountofwithdrawlrequested());
    entity.setPurposeofwithdrawl(payload.getPurposeofwithdrawl());
    entity.setWithdrawlrule(payload.getWithdrawlrule());
    entity.setConcernedofficername(payload.getConcernedofficername());

    entity.setIspriorwithdrawlforsamepurpose(payload.getIspriorwithdrawlforsamepurpose());

    if (Boolean.TRUE.equals(payload.getIspriorwithdrawlforsamepurpose())) {
        entity.setPriorwithdrawlamount(payload.getPriorwithdrawlamount());
        entity.setPriorwithdrawlfinyear(payload.getPriorwithdrawlfinyear());
    } else {
        entity.setPriorwithdrawlamount(null);
        entity.setPriorwithdrawlfinyear(null);
    }

    /* ================= DO NOT TOUCH WORKFLOW ================= */
    // ❌ DO NOT change:
    // entity.setCurrentOwnerRole(...)
    // entity.setCurrentStep(...)
    // entity.setWorkflowId(...)

    /* ================= IMPORTANT ================= */

    // 🔥 DO NOT reset isReturned here
    // because your processApplications() handles resume logic

    detailsRepo.save(entity);
}


@Override
public Map<String, Object> getDetailsByPan(String pan) {
    try {
        System.out.println("==========================================================================");

        String user = Base64.getEncoder().encodeToString("CCBS".getBytes(StandardCharsets.UTF_8));
        String pass = Base64.getEncoder().encodeToString("CCBS@2026".getBytes(StandardCharsets.UTF_8));
        String panEnc = Base64.getEncoder().encodeToString(pan.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("User-Agent", "PostmanRuntime/7.56.1");
        headers.set("Accept", "*/*");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("emp_pan_no", panEnc);
        body.add("user_name", user);
        body.add("password", pass);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();

        // Debug outgoing request
        restTemplate.getInterceptors().add((req, reqBody, exec) -> {
            System.out.println("FINAL BODY:");
            System.out.println(new String(reqBody, StandardCharsets.UTF_8));
            return exec.execute(req, reqBody);
        });

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://gifmis.cga.gov.in/centralEIS/WebServiceSOAP/gpf_advance_withdrawal.php",
                request,
                String.class
        );

        String raw = response.getBody();

        System.out.println("RESPONSE:");
        System.out.println(raw);

        // ================= STEP 1: Validate raw =================
        if (raw == null || raw.isEmpty()) {
            throw new RuntimeException("Empty API response");
        }

        if (raw.contains("StatusCode")) {
            throw new RuntimeException("API Error: " + raw);
        }

        // ================= STEP 2: Parse JSON =================
        ObjectMapper mapper = new ObjectMapper();
        com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(raw);

        String status = root.path("status").asText();
        String message = root.path("message").asText();

        // ================= STEP 3: Handle NO DATA =================
        if ("OK".equalsIgnoreCase(status) &&
            "No data found".equalsIgnoreCase(message)) {

            System.out.println("No data found for PAN: " + pan);

            Map<String, Object> result = new HashMap<>();
            result.put("status", status);
            result.put("message", message);
            result.put("data", Collections.emptyList());

            return result;
        }

        // ================= STEP 4: Parse actual data =================
        com.fasterxml.jackson.databind.JsonNode dataNode = root.path("data");

        if (dataNode.isMissingNode() || !dataNode.isArray() || dataNode.size() == 0) {
            throw new RuntimeException("Invalid API response: data list missing");
        }

        com.fasterxml.jackson.databind.JsonNode outer = dataNode.get(0);

        if (!outer.isArray() || outer.size() == 0) {
            throw new RuntimeException("Invalid API response: inner list missing");
        }

        com.fasterxml.jackson.databind.JsonNode detail = outer.get(0);

        if (!detail.isObject()) {
            throw new RuntimeException("Invalid API response: detail object missing");
        }

        // Convert JsonNode → Map
        return mapper.convertValue(detail, new TypeReference<Map<String, Object>>() {});

    } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Error calling GPF API", e);
    }
}


private Map<String, Object> parseSoapResponse(String xml) {

    Map<String, Object> result = new HashMap<>();

    try {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        InputStream is = new ByteArrayInputStream(xml.getBytes());
        Document doc = builder.parse(is);

        doc.getDocumentElement().normalize();

        // 🔥 Example extraction (adjust based on actual XML tags)

        result.put("gpfaccountno", getTagValue(doc, "gpfaccountno"));
        result.put("basicpay", getTagValue(doc, "basicpay"));
        result.put("closingbalance", getTagValue(doc, "closingbalance"));
        result.put("totalcreditamount", getTagValue(doc, "totalcreditamount"));
        result.put("refundafterdateofoutstandingbalance", getTagValue(doc, "refundamount"));
        result.put("totalwithdrawlamount", getTagValue(doc, "totalwithdrawlamount"));
        result.put("netbalance", getTagValue(doc, "netbalance"));
        result.put("outstandingbalance", getTagValue(doc, "outstandingbalance"));
        result.put("nameoftheofficermaintainingthePFAccount",getTagValue(doc, "officername"));

        {/*result.put("ispriorwithdrawalforsamepurpose",
                getTagValue(doc, "ispriorwithdrawal"));

        result.put("priorwithdrawalamount",
                getTagValue(doc, "priorwithdrawalamount"));

        result.put("priorwithdrawalfinyear",
    getTagValue(doc, "priorwithdrawalyear"));*/}

    } catch (Exception e) {
        e.printStackTrace();
    }

    return result;
}
private Map<String, Object> getMockDetails(String pan) {

    Map<String, Object> result = new HashMap<>();

    result.put("gpfaccountno", "GPF123456");
    result.put("basicpay", "75000");
    result.put("closingbalance", "1250000");
    result.put("totalcreditamount", "250000");
    result.put("refundafterdateofoutstandingbalance", "15000");
    result.put("totalwithdrawlamount", "50000");
    result.put("netbalance", "1465000");
    result.put("outstandingbalance", "22000");

    result.put("nameoftheofficermaintainingthePFAccount", "Rajesh Kumar");

    result.put("ispriorwithdrawalforsamepurpose", "Yes");
    result.put("priorwithdrawalamount", "30000");
    result.put("priorwithdrawalfinyear", "2022-23");

    // 👇 Dynamic feel (optional)
    result.put("pan", pan);

    return result;
}
private String getTagValue(Document doc, String tagName) {
    NodeList nodeList = doc.getElementsByTagName(tagName);
    if (nodeList.getLength() == 0) return null;
    return nodeList.item(0).getTextContent();
}
}
