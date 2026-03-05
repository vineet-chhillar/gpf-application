package com.example.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.entity.ActionMaster;
import com.example.backend.entity.AdvanceApplicationStatusTrail;
import com.example.backend.entity.GpfAdvanceDetails;
import com.example.backend.entity.GpfAdvanceMaster;
import com.example.backend.entity.WorkflowTransition;
import com.example.backend.repository.AdvanceStatusTrailRepo;
import com.example.backend.repository.GpfAdvanceDetailsRepo;
import com.example.backend.repository.GpfAdvanceMasterRepo;
import com.example.backend.repository.ActionMasterRepository;
import com.example.backend.repository.WorkflowTransitionRepository;

@Service
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
    private WorkflowTransitionRepository workflowTransitionRepo;

    private LocalDate getFinancialYearStart() {

    LocalDate today = LocalDate.now();

    int year = today.getYear();
    int month = today.getMonthValue();

    // If Jan–Mar → FY started previous year
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

private Long determineWorkflow(String designation) {

    if (designation == null) return 3L;

    String d = designation.toUpperCase();

    if (d.equals("SCIENTIST-D") ||
        d.equals("SCIENTIST-E") ||
        d.equals("SCIENTIST-F") ||
        d.equals("SCIENTIST-G"))
        return 2L;

    if (d.equals("SCIENTIST-B") ||
        d.equals("SCIENTIST-C") ||
        d.equals("SECTION OFFICER"))
        return 1L;

    return 3L;
}
    public void saveAdvanceApplication(
        GpfAdvanceMaster master,
        GpfAdvanceDetails details,
        Long roleId,
        Long actionId) {

    System.out.println("ROLE ID = " + roleId);
    System.out.println("ACTION ID = " + actionId);

    if (actionId == null)
        throw new RuntimeException("Action Id missing");

    actionRepo.findById(actionId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid action"));

    Integer installments =
            details.getNoofmonthlyinstallmentsforpaymentofconsolidatedadvance();

    if (installments == null || installments <= 0)
        throw new RuntimeException("Installments must be provided");

    if (installments > 60)
        throw new RuntimeException("Installments cannot exceed 60");

    /* SAVE MASTER */

    master = masterRepo.save(master);

    /* PREPARE DETAILS */

    details.setApplicationId(master.getId());
    details.setCreatedat(LocalDateTime.now());

    details.setCreditfromdate(getFinancialYearStart());
    details.setCredittodate(LocalDate.now());

    details.setDateofoutstandingbalance(getLastFinancialYearEnd());

    details.setWithdrawlfromdate(getFinancialYearStart());
    details.setWithdrawltodate(LocalDate.now());

    details.setActionId(actionId);

    /* WORKFLOW */

    Long workflowId = determineWorkflow(master.getDesignation());
    details.setWorkflowId(workflowId);

    Long employeeRoleId = 28L;

    WorkflowTransition transition =
            workflowTransitionRepo
                    .findFirstByWorkflowIdAndFromRoleOrderByStepOrder(
                            workflowId,
                            employeeRoleId
                    )
                    .orElseThrow(() ->
                            new IllegalStateException("Workflow not configured"));

    details.setCurrentOwnerRole(transition.getToRole());
    details.setCurrentStep(transition.getStepOrder());

    /* SAVE DETAILS */

    detailsRepo.save(details);

    /* STATUS TRAIL */

    AdvanceApplicationStatusTrail trail =
            new AdvanceApplicationStatusTrail();

    trail.setApplicationid(master.getId());
    trail.setActionId(actionId);
    trail.setActionByRole(employeeRoleId);
    trail.setRemarks("Application Submitted");
    trail.setActionat(LocalDateTime.now());

    trailRepo.save(trail);
}
}
