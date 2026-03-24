package com.example.backend.service;
import java.util.List;

import com.example.backend.dto.ApplicationTrailDTO;
import com.example.backend.dto.GpfApplicationStatusResponseDTO;
import com.example.backend.dto.GpfWithdrawlRequestDTO;
import com.example.backend.dto.InboxApplicationDTO;
import com.example.backend.dto.WorkflowProcessRequestDTO;
import com.example.backend.entity.GpfWithdrawlDetails;
public interface GpfWithdrawlService {

    void saveWithdrawl(GpfWithdrawlRequestDTO dto);

    GpfWithdrawlRequestDTO getByEmpcode(String empcode);

    void processApplications(WorkflowProcessRequestDTO request);

    List<GpfWithdrawlDetails> getInboxByRole(Long roleId);

    GpfApplicationStatusResponseDTO getApplicationStatus(String empcode);

    List<ApplicationTrailDTO> getTrail(Long applicationId);

    List<GpfApplicationStatusResponseDTO> getAllApplicationStatus();

    Long getCurrentWorkflowRole();

    List<InboxApplicationDTO> getAllPendingApplications();

    // 🔥 ADD THIS
    void updateWithdrawal(Long id, GpfWithdrawlRequestDTO dto);
}
