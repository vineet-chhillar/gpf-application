package com.example.backend.repository;

import com.example.backend.entity.ApplicationStatusTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationStatusTrailRepository
        extends JpaRepository<ApplicationStatusTrail, Long> {


List<ApplicationStatusTrail>
findByApplicationIdOrderByActionatAsc(Long applicationId);

//List<ApplicationStatusTrail>
//findByApplicationIdOrderByCreatedAtAsc(Long applicationId);

}
