package com.example.backend.repository;
import com.example.backend.entity.GpfWithdrawlDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GpfWithdrawlDetailsRepository
        extends JpaRepository<GpfWithdrawlDetails, Long> {

    Optional<GpfWithdrawlDetails> findByEmpcode(String empcode);
}
