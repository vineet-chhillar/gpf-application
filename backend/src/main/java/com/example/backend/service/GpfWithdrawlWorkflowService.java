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

public List<AdminInboxDTO> getAdminInbox() {

    List<GpfWithdrawlDetails> apps =
            repo.findByCurrentOwnerRoleAndStatus_StatusCode(
                    "ADMIN", "SUBMITTED"
            );

    List<AdminInboxDTO> result = new ArrayList<>();

    for (GpfWithdrawlDetails app : apps) {

        AdminInboxDTO dto = new AdminInboxDTO();

        dto.setApplicationId(app.getId());
        dto.setEmpCode(app.getEmpcode());
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
                "ADMIN".equals(app.getCurrentOwnerRole())
                && "SUBMITTED".equals(app.getStatus().getStatusCode())
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




}
