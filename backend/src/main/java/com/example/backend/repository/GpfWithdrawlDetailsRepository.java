package com.example.backend.repository;
import com.example.backend.entity.ApplicationStatusTrail;
import com.example.backend.entity.GpfWithdrawlDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
public interface GpfWithdrawlDetailsRepository
        extends JpaRepository<GpfWithdrawlDetails, Long> {

    Optional<GpfWithdrawlDetails> 
findByMaster_Empcode(String empcode);


    List<GpfWithdrawlDetails>
    findByCurrentOwnerRole(Long role);

  List<GpfWithdrawlDetails> 
findByMaster_EmpcodeOrderByDateofapplicationDesc(String empcode);
Optional<GpfWithdrawlDetails> 
findFirstByCurrentOwnerRoleNotOrderByCurrentOwnerRoleAsc(Long role);

List<GpfWithdrawlDetails> findByCurrentOwnerRoleNot(Long roleId);

}
