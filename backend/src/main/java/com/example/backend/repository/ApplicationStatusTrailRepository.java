package com.example.backend.repository;



import com.example.backend.entity.ApplicationStatusTrail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationStatusTrailRepository
        extends JpaRepository<ApplicationStatusTrail, Long> {
}
