package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.entity.GpfAdvanceDetails;

@Repository
public interface GpfAdvanceDetailsRepo
        extends JpaRepository<GpfAdvanceDetails, Long> {


    Optional<GpfAdvanceDetails> findByMaster_Id(Long id);

List<GpfAdvanceDetails> findByCurrentOwnerRoleNot(Long role);
}
