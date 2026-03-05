package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.entity.AdvanceApplicationStatusTrail;


@Repository
public interface AdvanceStatusTrailRepo
        extends JpaRepository<AdvanceApplicationStatusTrail, Long> {

    List<AdvanceApplicationStatusTrail>
    findByApplicationidOrderByActionatAsc(Long applicationid);

     
}
