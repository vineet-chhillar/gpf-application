package com.example.backend.controller;
import com.example.backend.dto.GpfWithdrawlRequestDTO;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import com.example.backend.service.GpfWithdrawlService;


import jakarta.validation.Valid;




@RestController
@RequestMapping("/api/gpf-withdrawl")
public class GpfWithdrawlController {

    @Autowired
    private GpfWithdrawlService gpfWithdrawlService;

    @PostMapping("/save")
    public ResponseEntity<?> save(@Valid @RequestBody GpfWithdrawlRequestDTO dto) {
        try {
            gpfWithdrawlService.saveWithdrawl(dto);
            return ResponseEntity.ok("GPF Withdrawl Application Saved Successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
}

{/*@PostMapping("/save")
public ResponseEntity<?> save(@RequestBody GpfWithdrawlRequestDTO dto) {
    System.out.println("GPF DATA RECEIVED: " + dto);
    return ResponseEntity.ok("Saved");
}*/}



    @GetMapping("/{empcode}")
    public ResponseEntity<?> getByEmpcode(@PathVariable String empcode) {
        try {
            return ResponseEntity.ok(gpfWithdrawlService.getByEmpcode(empcode));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}


