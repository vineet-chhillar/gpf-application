package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.StatusMaster;

import java.util.Optional;

public interface StatusMasterRepository extends JpaRepository<StatusMaster, Long> {

    Optional<StatusMaster> findByStatusCode(String statusCode);
}

