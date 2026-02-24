package com.example.backend.repository;

import com.example.backend.entity.ActionMaster;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionMasterRepository extends JpaRepository<ActionMaster, Long> {

    List<ActionMaster> findByIsActiveTrue();
    
}
