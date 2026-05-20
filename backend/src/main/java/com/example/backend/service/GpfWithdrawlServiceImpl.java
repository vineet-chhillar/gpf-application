package com.example.backend.service;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

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

import com.example.backend.repository.ActionMasterRepository;
import com.example.backend.repository.ApplicationStatusTrailRepository;
import com.example.backend.repository.FunctionalRoleRepository;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
//import java.net.http.HttpHeaders;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.util.Base64;

@Service
@Transactional
public class GpfWithdrawlServiceImpl implements GpfWithdrawlService {


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
   

@Override
public void saveWithdrawl(GpfWithdrawlRequestDTO dto) {

    if (dto == null || dto.getMaster() == null || dto.getDetails() == null) {
        throw new IllegalArgumentException("Invalid request payload");
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
        throw new IllegalArgumentException("Empcode is mandatory");
    }

    List<GpfWithdrawlDetails> activeApps =
    detailsRepo.findByMaster_EmpcodeAndCurrentOwnerRoleNot(empcode, 0L);

if (!activeApps.isEmpty()) {
    throw new IllegalStateException(
        "An application is already under process for this employee"
    );
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
// 🔥 CANCEL / REJECT FLOW (HIGHEST PRIORITY)
if (action.getActionId() == 12L) {

    System.out.println("🔥 CANCEL / REJECT IN PROCESS");

    details.setCurrentOwnerRole(0L);  // ✅ completed
    details.setCurrentStep(0);        // optional cleanup
    details.setAction(action);

    detailsRepo.save(details);

    // ✅ TRAIL
    ApplicationStatusTrail trail = new ApplicationStatusTrail();
    trail.setApplicationId(details.getMaster().getId());
    trail.setActionId(action.getActionId());
    trail.setActionByRole(actingRole);
    trail.setRemarks(request.getRemarks());

    trailRepo.save(trail);

    continue; // 🔥 VERY IMPORTANT
}
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

        System.out.println("STEP 1");

String innerXml =
          "<gpf_advance_withdrawl>"
        + "<user_name>CCBS</user_name>"
        + "<password>CCBS@2026</password>"
        + "<emp_pan_no>" + pan + "</emp_pan_no>"
        + "</gpf_advance_withdrawl>";

System.out.println("STEP 2");

String encodedPayload = Base64.getEncoder()
        .encodeToString(innerXml.getBytes("UTF-8"));

System.out.println("STEP 3");

String url =
        "https://training.gifmis.cga.gov.in/centralEIS/WebServiceSOAP/gpf_advance_withdrawal.php"
        + "?arg0="
        + URLEncoder.encode(encodedPayload, "UTF-8");

System.out.println("STEP 4");

RestTemplate restTemplate = new RestTemplate();

System.out.println("STEP 5");

ResponseEntity<String> response =
        restTemplate.getForEntity(url, String.class);

System.out.println("STEP 6");

        System.out.println("RAW RESPONSE:");
        System.out.println(response.getBody());

        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> apiResponse = mapper.readValue(
                response.getBody(),
                new TypeReference<Map<String, Object>>() {}
        );

        Object dataObj = apiResponse.get("data");

        if (!(dataObj instanceof List<?> outerList)
                || outerList.isEmpty()) {

            throw new RuntimeException("Invalid API response: outer list missing");
        }

        Object innerObj = outerList.get(0);

        if (!(innerObj instanceof List<?> innerList)
                || innerList.isEmpty()) {

            throw new RuntimeException("Invalid API response: inner list missing");
        }

        Object detailObj = innerList.get(0);

        if (!(detailObj instanceof Map<?, ?>)) {

            throw new RuntimeException("Invalid API response: detail object missing");
        }

        return (Map<String, Object>) detailObj;

   } catch (Exception e) {

    e.printStackTrace();

    throw new RuntimeException(e);
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
