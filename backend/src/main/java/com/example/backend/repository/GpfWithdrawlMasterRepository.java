package com.example.backend.repository;
import com.example.backend.entity.GpfWithdrawlMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface GpfWithdrawlMasterRepository
        extends JpaRepository<GpfWithdrawlMaster, Long> {

    Optional<GpfWithdrawlMaster> findByEmpcode(String empcode);
}

