package com.example.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.AdvanceRequestDTO;
import com.example.backend.service.GpfAdvanceService;

@RestController
@RequestMapping("/api/gpf-advance")
public class GpfAdvanceController {

    @Autowired
    private GpfAdvanceService service;

    @PostMapping("/save")
public ResponseEntity<?> saveAdvance(@RequestBody AdvanceRequestDTO dto) {

    try {

        System.out.println("===== SAVE ADVANCE API HIT =====");
        System.out.println("MASTER : " + dto.getMaster());
        System.out.println("DETAILS : " + dto.getDetails());
        System.out.println("ROLE ID : " + dto.getRoleId());
        System.out.println("ACTION ID : " + dto.getActionId());

    

        service.saveAdvanceApplication(
                dto.getMaster(),
                dto.getDetails(),
                dto.getRoleId(),
                dto.getActionId());

        return ResponseEntity.ok("Advance application saved");

    } 
    catch (Exception e) {

        System.out.println("===== ERROR SAVING ADVANCE =====");
        e.printStackTrace();

        return ResponseEntity.status(500).body(e.getMessage());
    }
}
}