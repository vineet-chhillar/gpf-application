package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.entity.ActionMaster;
import com.example.backend.entity.GpfAdvanceMaster;

@Repository
public interface GpfAdvanceMasterRepo
        extends JpaRepository<GpfAdvanceMaster, Long> {

        List<GpfAdvanceMaster> findByEmpcodeOrderByIdDesc(String empcode);
}
