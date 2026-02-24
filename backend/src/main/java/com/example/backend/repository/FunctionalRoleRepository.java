package com.example.backend.repository;

import com.example.backend.entity.FunctionalRoleMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FunctionalRoleRepository
        extends JpaRepository<FunctionalRoleMaster, Long> {
}