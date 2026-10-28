package com.example.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.ApiResponse;
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
import com.example.backend.repository.GpfWithdrawlDetailsRepository;
import com.example.backend.repository.GpfAdvanceRuleRepo;
import com.example.backend.repository.GpfSanctionOrderRepository;
import com.example.backend.repository.ActionMasterRepository;
import com.example.backend.repository.WorkflowTransitionRepository;
import com.example.backend.dto.GpfEmployeeDetailsDTO;
import com.example.backend.dto.FunctionalRoleDTO;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpSession;
import com.example.backend.service.GpfEmployeeService;

import java.util.Map;

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

    @Autowired
    private GpfSanctionOrderRepository gpfSanctionOrderRepository;

    @Autowired
    private GpfWithdrawlDetailsRepository withdrawlDetailsRepo;

    

    /* ================= FINANCIAL YEAR METHODS ================= */
@Autowired
private HttpSession session;


private final GpfEmployeeService employeeService;

    public GpfAdvanceService(
            GpfEmployeeService employeeService) {
        this.employeeService = employeeService;
    }
   private GpfEmployeeDetailsDTO getLoggedInEmployee() {

    Object employeeDetails =
            session.getAttribute("GPF_EMPLOYEE_DETAILS");

    if (!(employeeDetails instanceof GpfEmployeeDetailsDTO employee)) {
        throw new IllegalStateException(
                "Logged-in employee details not found in session"
        );
    }

    return employee;
}

private String getLoggedInEmpCode() {

    GpfEmployeeDetailsDTO employee = getLoggedInEmployee();

    if (employee.getEmpCode() == null) {
        throw new IllegalStateException(
                "Logged-in employee code not found in session"
        );
    }

    return String.valueOf(employee.getEmpCode());
}

private Set<String> getLoggedInRoleDivisionAssignments() {

    GpfEmployeeDetailsDTO employee = getLoggedInEmployee();

    if (employee.getFunctionalRoles() == null
            || employee.getFunctionalRoles().isEmpty()) {
        throw new IllegalStateException(
                "No functional roles found for logged-in employee"
        );
    }

    return employee.getFunctionalRoles().stream()
            .filter(role -> role.getPostId() != null)
            .filter(role -> role.getDivCode() != null
                    && !role.getDivCode().isBlank())
            .map(role -> role.getPostId()
                    + "|" + role.getDivCode().trim())
            .collect(Collectors.toSet());
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

    public ApiResponse<?> saveAdvanceApplication(
        GpfAdvanceMaster master,
        GpfAdvanceDetails details,
        Map<String, Object> ruleSpecificData,
        Long roleId,
        Long actionId) {

    System.out.println("===== ADVANCE SERVICE START =====");

    try {

        /* ================= BASIC VALIDATION ================= */

        if (master == null || details == null) {
            return ApiResponse.failure("Invalid request payload");
        }

        if (roleId == null) {
            return ApiResponse.failure("RoleId is mandatory");
        }

        if (actionId == null) {
            return ApiResponse.failure("ActionId is mandatory");
        }

        String empcode = master.getEmpcode();

        if (empcode == null || empcode.isBlank()) {
            return ApiResponse.failure("Empcode is mandatory");
        }

        /* ================= CHECK ACTIVE APPLICATIONS ================= */

        boolean activeAdvance = detailsRepo.existsActiveAdvance(empcode);

        if (activeAdvance) {
            return ApiResponse.failure(
                "An advance application is already under process for this employee"
            );
        }

        boolean activeWithdrawal = withdrawlDetailsRepo.existsActiveWithdrawal(empcode);

        if (activeWithdrawal) {
            return ApiResponse.failure(
                "A withdrawal application is already under process for this employee. " +
                "An advance application cannot be submitted until it is completed or cancelled."
            );
        }

        /* ================= FIELD VALIDATION ================= */

        if (details.getGpfaccountno() == null || details.getGpfaccountno().isBlank()) {
            return ApiResponse.failure("GPF Account No is mandatory");
        }

        if (details.getDateofapplication() == null) {
            return ApiResponse.failure("Date of application is mandatory");
        }

        Integer installments =
                details.getNoofmonthlyinstallmentsforpaymentofconsolidatedadvance();

        if (installments == null || installments <= 0) {
            return ApiResponse.failure("Installments must be provided");
        }

        if (installments > 60) {
            return ApiResponse.failure("Installments cannot exceed 60");
        }

        /* ================= RULE VALIDATION ================= */

        Long ruleId = details.getAdvancerule();

        if (ruleId != null && ruleSpecificData != null) {

            switch (ruleId.intValue()) {

                case 1:
                    if (ruleSpecificData.get("location") == null)
                        return ApiResponse.failure("Location required");

                    if (ruleSpecificData.get("ownershipType") == null)
                        return ApiResponse.failure("Ownership type required");
                    break;

                case 2:
                    if (ruleSpecificData.get("childName") == null)
                        return ApiResponse.failure("Child name required");
                    break;

                case 3:
                    if (ruleSpecificData.get("patientDetails") == null)
                        return ApiResponse.failure("Patient details required");
                    break;
            }
        }

        System.out.println("Validation passed");

        /* ================= FETCH ACTION ================= */

        ActionMaster action = actionRepo.findById(actionId).orElse(null);

        if (action == null) {
            return ApiResponse.failure("Invalid action id");
        }

        /* ================= SAVE DATA ================= */

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
                        .orElse(null);

        if (transition == null) {
            return ApiResponse.failure("Workflow not configured properly");
        }

        details.setCurrentOwnerRole(transition.getToRole());
        details.setCurrentStep(transition.getStepOrder());

        if (ruleSpecificData != null) {
            details.setRuleSpecificDataJson(ruleSpecificData);
        }

        detailsRepo.save(details);

        /* ================= TRAIL ================= */

        AdvanceApplicationStatusTrail trail = new AdvanceApplicationStatusTrail();

        trail.setApplicationid(master.getId());
        trail.setActionId(action.getActionId());
        trail.setActionByRole(roleId);
        trail.setRemarks("Application Submitted");
        trail.setActionat(LocalDateTime.now());
//new columns

trail.setActionByEmpCode(getLoggedInEmpCode());
trail.setActionByDivCode(transition.getToRoleDivCode());

        trailRepo.save(trail);

        /* ================= SUCCESS ================= */

        return ApiResponse.success(
            "Advance application submitted successfully",
            master.getId()
        );

    } catch (Exception e) {

        e.printStackTrace();

        return ApiResponse.failure("Failed to save advance application");
    }
}

public List<InboxApplicationDTO> getAllPendingApplications() {

    // 1. Logged-in employee's assignments
    Set<String> assignments =
            new HashSet<>(getLoggedInRoleDivisionAssignments());

    // 2. Check whether logged-in employee is a Link Officer
    Object employeeDetails =
            session.getAttribute("GPF_EMPLOYEE_DETAILS");

    if (employeeDetails instanceof GpfEmployeeDetailsDTO employee
            && employee.getFunctionalRoles() != null) {

        boolean isLinkOfficer =
                employee.getFunctionalRoles().stream()
                        .anyMatch(role ->
                                role.getPostId() != null
                                && role.getPostId().longValue() == 13L
                        );

        // 3. Add linked employee's assignments
        if (isLinkOfficer) {

            Long empCode = employee.getEmpCode();

            employeeService.getLinkedEmployeeCode(empCode)
                    .flatMap(employeeService::getEmployeeDetails)
                    .ifPresent(linkedEmployee -> {

                        if (linkedEmployee.getFunctionalRoles() != null) {

                            linkedEmployee.getFunctionalRoles().stream()
                                    .filter(role -> role.getPostId() != null)
                                    .filter(role -> role.getDivCode() != null
                                            && !role.getDivCode().isBlank())
                                    .map(role -> role.getPostId()
                                            + "|" + role.getDivCode().trim())
                                    .forEach(assignments::add);
                        }
                    });
        }
    }

    // 4. Fetch pending advance applications
    List<GpfAdvanceDetails> list =
            detailsRepo.findByCurrentOwnerRoleNot(0L);

    System.out.println(
            "Advance inbox total before filtering = " + list.size()
    );

    // 5. Apply existing workflow filters and map DTOs
    return list.stream()

            .filter(d -> d.getCurrentOwnerRole() != null
                    && d.getWorkflowId() != null
                    && d.getCurrentStep() != null
                    && d.getCurrentStep() > 0)

            .filter(d -> {

                String divisionCode =
                        workflowTransitionRepo
                                .findFirstByWorkflowIdAndStepOrderOrderByStepOrder(
                                        d.getWorkflowId(),
                                        d.getCurrentStep()
                                )
                                .map(WorkflowTransition::getToRoleDivCode)
                                .orElse(null);

                if (divisionCode == null || divisionCode.isBlank()) {
                    return false;
                }

                String assignment =
                        d.getCurrentOwnerRole()
                                + "|" + divisionCode.trim();

                return assignments.contains(assignment);
            })

            .map(d -> {

                InboxApplicationDTO dto = new InboxApplicationDTO();

                if (d.getMaster() != null) {
                    dto.setApplicationId(d.getMaster().getId());
                    dto.setEmpCode(d.getMaster().getEmpcode());
                    dto.setEmployeeName(d.getMaster().getEmpname());
                    dto.setDesignation(d.getMaster().getDesignation());
                }

                dto.setAmount(d.getAmountofadvancerequested());
                dto.setPurpose(d.getPurposeofadvance());
                dto.setCurrentOwnerRoleId(d.getCurrentOwnerRole());

                if (d.getDateofapplication() != null) {
                    dto.setApplicationDate(
                            d.getDateofapplication().toString()
                    );
                }

                String roleName =
                        roleRepo.findById(d.getCurrentOwnerRole())
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

public GpfApplicationStatusResponseDTO getApplicationStatus(String empcode) {

   List<GpfAdvanceMaster> masters =
        masterRepo.findByEmpcodeOrderByIdDesc(empcode);

if (masters.isEmpty()) {
    throw new RuntimeException("Application not found");
}

GpfAdvanceMaster master = masters.get(0); // ✅ latest

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
// Current division name
String currentDivisionName = "-";

if (details.getWorkflowId() != null
        && details.getCurrentStep() != null
        && details.getCurrentStep() > 0) {

    currentDivisionName =
            workflowTransitionRepo.findDivisionNameByWorkflowAndStep(
                    details.getWorkflowId(),
                    details.getCurrentStep()
            ).orElse("-");
}

    GpfApplicationStatusResponseDTO response =
            new GpfApplicationStatusResponseDTO();

    response.setMaster(master);
    response.setDetails(details);
    response.setTrail(trailDTOs);
response.setCurrentDivisionName(currentDivisionName);

//System.out.println("check div name " + currentDivisionName);
    return response;
}
public List<ApplicationTrailDTO> getTrail(Long applicationId) {

    List<AdvanceApplicationStatusTrail> trails =
            trailRepo.findByApplicationidOrderByActionatAsc(applicationId);

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

            // Temporary hardcoded employee code
String actionByEmpCode = getLoggedInEmpCode();

// Capture the current workflow step before changing it
Integer actingStep = details.getCurrentStep();

// Fetch the transition for the current step
WorkflowTransition actingTransition =
    workflowTransitionRepo
        .findFirstByWorkflowIdAndStepOrderOrderByStepOrder(
            workflowId,
            actingStep
        )
        .orElseThrow(() ->
            new IllegalStateException(
                "Workflow transition not found for workflowId="
                    + workflowId + ", stepOrder=" + actingStep
            )
        );

String actionByDivCode = actingTransition.getToRoleDivCode();

            // 🔥 RESUME FLOW (HIGHEST PRIORITY)
            if (Boolean.TRUE.equals(details.getIsReturned())) {

                System.out.println("🔥 ADVANCE RESUME FLOW");

            // 🔥 CANCEL / REJECT FLOW (HIGHEST PRIORITY AFTER RESUME)
  

                // ✅ RETURN TO SAME ROLE WHO SENT BACK
              Integer resumeStep = details.getReturnFromStep();

WorkflowTransition next =
    workflowTransitionRepo
        .findFirstByWorkflowIdAndStepOrderGreaterThanOrderByStepOrder(
            workflowId,
            resumeStep - 2   // 🔥 SAME FIX AS WITHDRAWAL
        )
        .orElseThrow(() -> new IllegalStateException("Next workflow step missing"));

details.setCurrentOwnerRole(next.getToRole());
details.setCurrentStep(next.getStepOrder());

// reset flags
details.setIsReturned(false);
details.setReturnFromStep(null);

details.setActionId(action.getActionId());

                // ✅ RESET FLAGS
                details.setIsReturned(false);
                details.setReturnFromStep(null);

                details.setActionId(action.getActionId());

                

                detailsRepo.save(details);

                AdvanceApplicationStatusTrail trail =
                        new AdvanceApplicationStatusTrail();

                trail.setApplicationid(details.getMaster().getId());
                trail.setActionId(action.getActionId());
                trail.setActionByRole(actingRole);
                trail.setRemarks(request.getRemarks());
                trail.setActionat(LocalDateTime.now());
trail.setActionByEmpCode(actionByEmpCode);
trail.setActionByDivCode(actionByDivCode);
                trailRepo.save(trail);

                continue; // 🔥 VERY IMPORTANT
            }
if (action.getActionId() == 12L) {

    System.out.println("🔥 ADVANCE CANCEL / REJECT");

    details.setCurrentOwnerRole(0L);   // ✅ mark completed
    details.setCurrentStep(0);         // optional cleanup
    details.setActionId(action.getActionId());

    // reset flags (important)
    details.setIsReturned(false);
    details.setReturnFromStep(null);

    detailsRepo.save(details);

    // ✅ TRAIL
    AdvanceApplicationStatusTrail trail =
            new AdvanceApplicationStatusTrail();

    trail.setApplicationid(details.getMaster().getId());
    trail.setActionId(action.getActionId());
    trail.setActionByRole(actingRole);
    trail.setRemarks(request.getRemarks());
    trail.setActionat(LocalDateTime.now());
trail.setActionByEmpCode(actionByEmpCode);
trail.setActionByDivCode(actionByDivCode);
    trailRepo.save(trail);

    continue; // 🔥 VERY IMPORTANT
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

                System.out.println("🔥 ADVANCE SEND BACK FLOW");

                // ✅ FIXED (IMPORTANT)
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
                details.setActionId(action.getActionId());

            } else {

                // ✅ NORMAL FLOW

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

                // 🔥 SAFETY RESET
                details.setIsReturned(false);
                details.setReturnFromStep(null);
            }

            detailsRepo.save(details);

            AdvanceApplicationStatusTrail trail =
                    new AdvanceApplicationStatusTrail();

            trail.setApplicationid(details.getMaster().getId());
            trail.setActionId(action.getActionId());
            trail.setActionByRole(actingRole);
            trail.setRemarks(request.getRemarks());
            trail.setActionat(LocalDateTime.now());
trail.setActionByEmpCode(actionByEmpCode);
trail.setActionByDivCode(actionByDivCode);
            trailRepo.save(trail);
        }

    } catch (Exception e) {

        System.err.println("Error while processing advance workflow");
        e.printStackTrace();

        throw new RuntimeException("Workflow processing failed: " + e.getMessage());
    }
}
public List<GpfApplicationStatusResponseDTO> getAdvanceStatusByEmp(String empcode) {

    List<GpfAdvanceMaster> masters =
            masterRepo.findByEmpcodeOrderByIdDesc(empcode);

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
            response.setCurrentOwnerRoleId(details.getCurrentOwnerRole());
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

        /* ===== CURRENT STATUS ===== */

        String currentRoleName = "-";

        if (details != null && details.getCurrentOwnerRole() != null) {

            Long lastActionId = null;

            if (!trails.isEmpty()) {
                AdvanceApplicationStatusTrail last =
                        trails.get(trails.size() - 1);
                lastActionId = last.getActionId();
            }

            if (details.getCurrentOwnerRole() == 0) {

                if (lastActionId != null && lastActionId == 12L) {
                    currentRoleName = "Cancelled/Rejected";
                } else {
                    currentRoleName = "Completed";
                }

            } else {
                currentRoleName =
                        resolveRoleName(details.getCurrentOwnerRole());
            }
        }

        response.setCurrentOwnerRole(currentRoleName);

        /* ===== TRAIL DTO ===== */

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
/* ===== CURRENT DIVISION NAME ===== */

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

response.setCurrentDivisionName(currentDivisionName);


        return response;

    }).toList();
}
public List<GpfApplicationStatusResponseDTO> getAllApplicationStatus() {

    List<GpfAdvanceMaster> masters = masterRepo.findAll();

    return masters.stream().map(master -> {

        GpfApplicationStatusResponseDTO response = new GpfApplicationStatusResponseDTO();

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

    response.setCurrentOwnerRoleId(details.getCurrentOwnerRole());

   
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
String currentRoleName = "-";

if (details != null && details.getCurrentOwnerRole() != null) {

    Long lastActionId = null;

    if (!trails.isEmpty()) {
        AdvanceApplicationStatusTrail last =
                trails.get(trails.size() - 1);
        lastActionId = last.getActionId();
    }

    if (details.getCurrentOwnerRole() == 0) {

        if (lastActionId != null && lastActionId == 12L) {
            currentRoleName = "Cancelled/Rejected";   // 🔥 FIX
        } else {
            currentRoleName = "Completed";
        }

    } else {
        currentRoleName =
                resolveRoleName(details.getCurrentOwnerRole());
    }
}

response.setCurrentOwnerRole(currentRoleName);
// ===== CURRENT DIVISION NAME =====

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

response.setCurrentDivisionName(currentDivisionName);
          boolean sanctionExists =  gpfSanctionOrderRepository.existsByApplicationId(master.getId());
          response.setSanctionGenerated(sanctionExists);


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

public void updateAdvanceApplication(
        Long id,
        GpfAdvanceDetails payload,
        Map<String, Object> ruleSpecificData) {

    try {

        GpfAdvanceDetails entity = detailsRepo.findByMaster_Id(id)
                .orElseThrow(() ->
                        new IllegalStateException("Application not found: " + id));

        // 🔥 ONLY ALLOW IF RETURNED
        if (!Boolean.TRUE.equals(entity.getIsReturned())) {
            throw new IllegalStateException("Editing allowed only for returned applications");
        }

        if (payload == null) {
            throw new IllegalArgumentException("Details payload is required");
        }

        /* ================= BASIC VALIDATION ================= */

        if (payload.getAmountofadvancerequested() == null ||
            payload.getAmountofadvancerequested().compareTo(java.math.BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException("Advance amount must be greater than 0");
        }

        if (payload.getPurposeofadvance() == null ||
            payload.getPurposeofadvance().isBlank()) {

            throw new IllegalArgumentException("Purpose is required");
        }

        if (payload.getAdvancerule() == null) {
            throw new IllegalArgumentException("Advance rule is required");
        }

        Integer installments =
                payload.getNoofmonthlyinstallmentsforpaymentofconsolidatedadvance();

        if (installments == null || installments <= 0) {
            throw new IllegalArgumentException("Installments must be provided");
        }

        if (installments > 60) {
            throw new IllegalArgumentException("Installments cannot exceed 60");
        }

        /* ================= RULE VALIDATION ================= */

        Map<String, Object> ruleData = ruleSpecificData;

        if (payload.getAdvancerule() != null && ruleData != null) {

            switch (payload.getAdvancerule().intValue()) {

                case 1: // HOUSE
                    if (ruleData.get("location") == null)
                        throw new IllegalArgumentException("Location required");

                    if (ruleData.get("ownershipType") == null)
                        throw new IllegalArgumentException("Ownership type required");
                    break;

                case 2: // EDUCATION
                    if (ruleData.get("childName") == null)
                        throw new IllegalArgumentException("Child name required");
                    break;

                case 3: // MEDICAL
                    if (ruleData.get("patientDetails") == null)
                        throw new IllegalArgumentException("Patient details required");
                    break;
            }
        }

        /* ================= UPDATE FIELDS ================= */

        entity.setAmountofadvancerequested(payload.getAmountofadvancerequested());
        entity.setPurposeofadvance(payload.getPurposeofadvance());
        entity.setParticulars(payload.getParticulars());
        entity.setAdvancerule(payload.getAdvancerule());

        entity.setNoofmonthlyinstallmentsforpaymentofconsolidatedadvance(
                payload.getNoofmonthlyinstallmentsforpaymentofconsolidatedadvance()
        );

        /* ================= JSON ================= */

        if (ruleData != null) {
            entity.setRuleSpecificDataJson(ruleData);
        }

        /* ================= IMPORTANT ================= */

    

        detailsRepo.save(entity);

    } catch (Exception e) {

        e.printStackTrace();
        throw new RuntimeException("Failed to update advance: " + e.getMessage());
    }
}
}