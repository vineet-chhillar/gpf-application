package com.example.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import com.example.backend.dto.AdminInboxDTO;
import com.example.backend.dto.ApplicationDetailsDTO;

import com.example.backend.entity.GpfWithdrawlDetails;
import com.example.backend.entity.GpfWithdrawlMaster;
import com.example.backend.entity.StatusMaster;
import com.example.backend.entity.ApplicationStatusTrail;

import com.example.backend.repository.GpfWithdrawlDetailsRepository;
import com.example.backend.repository.GpfWithdrawlMasterRepository;
import com.example.backend.repository.StatusMasterRepository;
import com.example.backend.repository.ApplicationStatusTrailRepository;
import com.example.backend.service.GpfWithdrawlRuleService;
import com.example.backend.entity.GpfWithdrawlRule;
import com.example.backend.dto.EmployeeInboxDTO;
import com.example.backend.dto.ApplicationStatusTrailDTO;
@Service
public class GpfWithdrawlWorkflowService {

   private static final String ACTING_ROLE = "ADMIN";

   private final GpfWithdrawlDetailsRepository repo;
private final GpfWithdrawlMasterRepository masterRepo;
private final StatusMasterRepository statusRepo;
private final ApplicationStatusTrailRepository trailRepo;
private final GpfWithdrawlRuleService ruleService;



 @Autowired
public GpfWithdrawlWorkflowService(
        GpfWithdrawlDetailsRepository repo,
        GpfWithdrawlMasterRepository masterRepo,
        StatusMasterRepository statusRepo,
        ApplicationStatusTrailRepository trailRepo,
        GpfWithdrawlRuleService ruleService) {

    this.repo = repo;
    this.masterRepo = masterRepo;
    this.statusRepo = statusRepo;
    this.trailRepo = trailRepo;
    this.ruleService = ruleService;
}

@Transactional
public void adminVerify(Long applicationId, String remarks) {


    if (remarks == null || remarks.trim().isEmpty()) {
        throw new IllegalStateException("Remarks are mandatory");
    }

    GpfWithdrawlDetails app = repo.findById(applicationId)
        .orElseThrow(() -> new RuntimeException("Application not found"));

    // 1️⃣ Final lock check
    if (app.getStatus().isFinal()) {
        throw new IllegalStateException("Finalized application cannot be modified");
    }

    // 2️⃣ Owner role check
    if (!"ADMIN".equals(app.getCurrentOwnerRole())) {
        throw new IllegalStateException("Application is not with ADMIN");
    }

    // 3️⃣ Status check
    if (!"SUBMITTED".equals(app.getStatus().getStatusCode())) {
        throw new IllegalStateException("Application is not in SUBMITTED state");
    }

    // 4️⃣ Load next status
StatusMaster nextStatus = statusRepo
        .findByStatusCode("ADMIN_VERIFIED")
        .orElseThrow(() ->
            new IllegalStateException("Status ADMIN_VERIFIED not found"));


    // 5️⃣ Update application
    app.setStatus(nextStatus);
    app.setCurrentOwnerRole("CASH");
    repo.save(app);

    // 6️⃣ Insert trail
    ApplicationStatusTrail trail = new ApplicationStatusTrail();
    trail.setApplicationid(app.getId());
    trail.setStatusid(nextStatus.getId());
    trail.setActionby("ADMIN");
    trail.setRemarks(remarks);

    trailRepo.save(trail);
}
@Transactional
public void cashVerify(Long applicationId, String remarks) {



    // 1️⃣ Remarks mandatory
    if (remarks == null || remarks.trim().isEmpty()) {
        throw new IllegalStateException("Remarks are mandatory");
    }

    // 2️⃣ Load application
    GpfWithdrawlDetails app = repo.findById(applicationId)
        .orElseThrow(() -> new RuntimeException("Application not found"));
System.out.println(
  "CASH VERIFY → id=" + applicationId +
  ", owner=" + app.getCurrentOwnerRole() +
  ", status=" + app.getStatus().getStatusCode()
);
    // 3️⃣ Final lock check
    if (app.getStatus().isFinal()) {
        throw new IllegalStateException("Finalized application cannot be modified");
    }

    // 4️⃣ Owner role check
    if (!"CASH".equals(app.getCurrentOwnerRole())) {
        throw new IllegalStateException("Application is not with CASH");
    }

    // 5️⃣ Status check
    if (!"ADMIN_VERIFIED".equals(app.getStatus().getStatusCode())) {
        throw new IllegalStateException(
            "Application is not in ADMIN_VERIFIED state"
        );
    }

    // 6️⃣ Load next status
    StatusMaster nextStatus = statusRepo
        .findByStatusCode("CASH_VERIFIED")
        .orElseThrow(() ->
            new IllegalStateException("Status CASH_VERIFIED not found")
        );

    // 7️⃣ Update application
    app.setStatus(nextStatus);
    app.setCurrentOwnerRole("ADMIN");
    repo.save(app);

    // 8️⃣ Insert audit trail
    ApplicationStatusTrail trail = new ApplicationStatusTrail();
    trail.setApplicationid(app.getId());
    trail.setStatusid(nextStatus.getId());
    trail.setActionby("CASH");
    trail.setRemarks(remarks);

    trailRepo.save(trail);
}
public List<AdminInboxDTO> getCashInbox() {

    
        List<GpfWithdrawlDetails> apps =
    repo.findByCurrentOwnerRoleAndStatus_StatusCodeIn(
        "CASH",
        List.of("ADMIN_VERIFIED")
    );


    List<AdminInboxDTO> result = new ArrayList<>();

    for (GpfWithdrawlDetails app : apps) {

        AdminInboxDTO dto = new AdminInboxDTO();

        dto.setApplicationId(app.getId());
        dto.setEmpCode(app.getEmpcode());
        // Load name from master
GpfWithdrawlMaster master = masterRepo
        .findByEmpcode(app.getEmpcode())
        .orElse(null);

dto.setEmpName(master != null ? master.getEmpname() : null);

        dto.setAmountRequested(app.getAmountofwithdrawlrequested());
        dto.setDateOfApplication(app.getDateofapplication());

        dto.setStatusCode(app.getStatus().getStatusCode());
        dto.setCurrentOwnerRole(app.getCurrentOwnerRole());

        dto.setCanView(true);
        dto.setCanVerify(
            "CASH".equals(app.getCurrentOwnerRole())
            && "ADMIN_VERIFIED".equals(app.getStatus().getStatusCode())
        );

        result.add(dto);
    }

    return result;
}

public List<AdminInboxDTO> getAdminInbox() {

    List<GpfWithdrawlDetails> apps =
    repo.findByCurrentOwnerRoleAndStatus_StatusCodeIn(
        "ADMIN",
        List.of("SUBMITTED", "CASH_VERIFIED")
    );


    List<AdminInboxDTO> result = new ArrayList<>();

    for (GpfWithdrawlDetails app : apps) {

        AdminInboxDTO dto = new AdminInboxDTO();

        dto.setApplicationId(app.getId());
        
       dto.setEmpCode(app.getEmpcode());

// Load name from master
GpfWithdrawlMaster master = masterRepo
        .findByEmpcode(app.getEmpcode())
        .orElse(null);

dto.setEmpName(master != null ? master.getEmpname() : null);


        dto.setAmountRequested(app.getAmountofwithdrawlrequested());
        dto.setDateOfApplication(app.getDateofapplication());

        dto.setStatusCode(app.getStatus().getStatusCode());
        dto.setCurrentOwnerRole(app.getCurrentOwnerRole());

        
        // ADMIN rules
        dto.setCanView(true);
        dto.setCanVerify(
    "ADMIN".equals(app.getCurrentOwnerRole()) &&
    (
        "SUBMITTED".equals(app.getStatus().getStatusCode()) ||
        "CASH_VERIFIED".equals(app.getStatus().getStatusCode())
    )
);

        result.add(dto);
    }

    return result;
}

public ApplicationDetailsDTO getApplicationDetails(Long id) {

    GpfWithdrawlDetails app = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found"));

    // 🔹 Load employee master (THIS IS WHERE IT GOES)
    GpfWithdrawlMaster master = masterRepo
            .findByEmpcode(app.getEmpcode())
            .orElseThrow(() -> new RuntimeException("Employee master not found"));

    ApplicationDetailsDTO dto = new ApplicationDetailsDTO();

    dto.setApplicationId(app.getId());

    // Employee (FROM MASTER)
    dto.setEmpCode(master.getEmpcode());
    dto.setEmpName(master.getEmpname());
    dto.setDesignation(master.getDesignation());
    dto.setDivision(master.getEmpdivision());

    // Withdrawal (FROM DETAILS)
    dto.setAmountRequested(app.getAmountofwithdrawlrequested());
    dto.setPurposeOfWithdrawal(app.getPurposeofwithdrawl());
    dto.setDateOfApplication(app.getDateofapplication());

    // Financial
    dto.setBasicPay(app.getBasicpay());
    dto.setOutstandingBalance(app.getOutstandingbalance());
    dto.setNetBalance(app.getNetbalance());
// ===== CREDIT DETAILS =====
dto.setCreditFromDate(app.getCreditfromdate());
dto.setCreditToDate(app.getCredittodate());
dto.setTotalCreditAmount(app.getTotalcreditamount());
dto.setRefundAfterOutstandingBalance(
        app.getRefundafterdateofoutstandingbalance()
);

// ===== WITHDRAWAL PERIOD DETAILS =====
dto.setWithdrawlFromDate(app.getWithdrawlfromdate());
dto.setWithdrawlToDate(app.getWithdrawltodate());
dto.setTotalWithdrawlAmount(app.getTotalwithdrawlamount());

    // Workflow
    dto.setStatusCode(app.getStatus().getStatusCode());
    dto.setCurrentOwnerRole(app.getCurrentOwnerRole());
// Withdrawal Rule (resolve ruleId → reason)
String withdrawalRuleText = null;

if (app.getWithdrawlrule() != null) {
    withdrawalRuleText = ruleService.getAllRules()
            .stream()
            .filter(r -> r.getRuleId().equals(app.getWithdrawlrule()))
            .map(GpfWithdrawlRule::getWithdrawlReason)
            .findFirst()
            .orElse(null);
}

dto.setWithdrawalRule(withdrawalRuleText);

    return dto;
}
public List<EmployeeInboxDTO> getEmployeeInbox(String empcode) {

    List<GpfWithdrawlDetails> apps =
        repo.findByEmpcodeOrderByDateofapplicationDesc(empcode);

    List<EmployeeInboxDTO> result = new ArrayList<>();

    for (GpfWithdrawlDetails app : apps) {

        EmployeeInboxDTO dto = new EmployeeInboxDTO();
        dto.setApplicationId(app.getId());
        dto.setDateOfApplication(app.getDateofapplication());
        dto.setAmountRequested(app.getAmountofwithdrawlrequested());
        dto.setStatusCode(app.getStatus().getStatusCode());
        dto.setCurrentOwnerRole(app.getCurrentOwnerRole());
        dto.setIsFinal(Boolean.TRUE.equals(app.getStatus().isFinal()));

        result.add(dto);
    }

    return result;
}

public List<ApplicationStatusTrailDTO> getStatusTrail(Long applicationId) {

    System.out.println("STATUS TRAIL → applicationId = " + applicationId);

    List<ApplicationStatusTrail> trails =
        trailRepo.findByApplicationidOrderByActionatAsc(applicationId);

    System.out.println("STATUS TRAIL → rows found = " + trails.size());

    List<ApplicationStatusTrailDTO> result = new ArrayList<>();

    for (ApplicationStatusTrail t : trails) {

        System.out.println(
            "TRAIL → id=" + t.getId() +
            ", statusid=" + t.getStatusid() +
            ", actionBy=" + t.getActionby()
        );

        ApplicationStatusTrailDTO dto = new ApplicationStatusTrailDTO();

        String statusCode = null;
        if (t.getStatusid() != null) {
            statusCode = statusRepo.findById(t.getStatusid())
                    .map(StatusMaster::getStatusCode)
                    .orElse("UNKNOWN_STATUS");
        }

        dto.setStatusCode(statusCode);
        dto.setActionBy(t.getActionby());
        dto.setRemarks(t.getRemarks());
        dto.setActionAt(t.getActionat());

        result.add(dto);
    }

    return result;
}



}
