package com.example.backend.service;

import com.example.backend.dto.GpfWithdrawlRequestDTO;
import com.example.backend.entity.GpfWithdrawlDetails;
import com.example.backend.entity.GpfWithdrawlMaster;
import com.example.backend.entity.StatusMaster;
import com.example.backend.repository.GpfWithdrawlDetailsRepository;
import com.example.backend.repository.GpfWithdrawlMasterRepository;
import com.example.backend.repository.StatusMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GpfWithdrawlServiceImpl implements GpfWithdrawlService {

    @Autowired
    private GpfWithdrawlMasterRepository masterRepo;

    @Autowired
    private GpfWithdrawlDetailsRepository detailsRepo;

    @Autowired
    private StatusMasterRepository statusRepo;

    @Override
    public void saveWithdrawl(GpfWithdrawlRequestDTO dto) {

        if (dto == null || dto.getMaster() == null || dto.getDetails() == null) {
            throw new IllegalArgumentException("Invalid request payload");
        }

        GpfWithdrawlMaster master = dto.getMaster();
        GpfWithdrawlDetails details = dto.getDetails();

        String empcode = master.getEmpcode();
        if (empcode == null || empcode.isBlank()) {
            throw new IllegalArgumentException("Empcode is mandatory");
        }

        // Prevent duplicate applications
        if (masterRepo.findByEmpcode(empcode).isPresent()) {
            throw new IllegalStateException(
                    "Withdrawal application already exists for this employee");
        }

        /* ================= SYNC FIELDS ================= */

        details.setEmpcode(empcode);

        if (details.getGpfaccountno() == null || details.getGpfaccountno().isBlank()) {
            throw new IllegalArgumentException("GPF Account No is mandatory");
        }

        if (details.getDateofapplication() == null) {
            throw new IllegalArgumentException("Date of application is mandatory");
        }

        /* ================= STATUS HANDLING ================= */

        if (details.getStatus() == null || details.getStatus().getId() == null) {
            throw new IllegalArgumentException("Status is mandatory");
        }

        StatusMaster status = statusRepo.findById(details.getStatus().getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid status id"));

        details.setStatus(status);

        /* ================= SAVE ================= */

        masterRepo.save(master);
        detailsRepo.save(details);
    }

    @Override
    public GpfWithdrawlRequestDTO getByEmpcode(String empcode) {

        if (empcode == null || empcode.isBlank()) {
            throw new IllegalArgumentException("Empcode is required");
        }

        GpfWithdrawlRequestDTO dto = new GpfWithdrawlRequestDTO();

        dto.setMaster(
                masterRepo.findByEmpcode(empcode)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Master record not found"))
        );

        dto.setDetails(
                detailsRepo.findByEmpcode(empcode)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Details record not found"))
        );

        return dto;
    }
}
