package com.example.backend.service;
import com.example.backend.dto.GpfWithdrawlRequestDTO;
public interface GpfWithdrawlService {

    void saveWithdrawl(GpfWithdrawlRequestDTO dto);

    GpfWithdrawlRequestDTO getByEmpcode(String empcode);
}
