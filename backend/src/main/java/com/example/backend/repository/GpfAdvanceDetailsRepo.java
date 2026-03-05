package com.example.backend.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.entity.GpfAdvanceDetails;

@Repository
public interface GpfAdvanceDetailsRepo
        extends JpaRepository<GpfAdvanceDetails, Long> {




}
