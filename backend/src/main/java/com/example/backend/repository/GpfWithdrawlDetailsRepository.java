package com.example.backend.repository;
import com.example.backend.entity.GpfWithdrawlDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
public interface GpfWithdrawlDetailsRepository
        extends JpaRepository<GpfWithdrawlDetails, Long> {

    List<GpfWithdrawlDetails> findByMaster_EmpcodeOrderByIdDesc(String empcode);


    List<GpfWithdrawlDetails>
    findByCurrentOwnerRole(Long role);

  List<GpfWithdrawlDetails> 
findByMaster_EmpcodeOrderByDateofapplicationDesc(String empcode);
Optional<GpfWithdrawlDetails> 
findFirstByCurrentOwnerRoleNotOrderByCurrentOwnerRoleAsc(Long role);

List<GpfWithdrawlDetails> findByCurrentOwnerRoleNot(Long roleId);
Optional<GpfWithdrawlDetails> findByMaster_Id(Long id);
//GpfWithdrawlDetails findTopByMaster_PannoOrderByIdDesc(String panno);

List<GpfWithdrawlDetails> findByMaster_EmpcodeAndCurrentOwnerRoleNot
(
   String empcode, Long roleId  
);
//boolean existsByMaster_EmpcodeAndCurrentOwnerRoleNot(String empcode, Long currentOwnerRole);
@Query("""
    SELECT COUNT(d) > 0
    FROM GpfWithdrawlDetails d
    WHERE d.master.empcode = :empcode
    AND d.currentOwnerRole <> 0
""")
boolean existsActiveWithdrawal(String empcode);
}
