package com.example.backend.controller;
import com.example.backend.dto.ApplicationTrailDTO;
import com.example.backend.dto.GpfApplicationStatusResponseDTO;
import com.example.backend.dto.GpfWithdrawlRequestDTO;
import com.example.backend.dto.WorkflowProcessRequestDTO;

import com.example.backend.repository.ActionMasterRepository;


import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import com.example.backend.service.GpfWithdrawlService;


import jakarta.validation.Valid;




@RestController
@RequestMapping("/api/gpf-withdrawl")
public class GpfWithdrawlController {


    @Autowired
    private GpfWithdrawlService service;

    @GetMapping("/status/{empcode}")
    public GpfApplicationStatusResponseDTO getStatus(@PathVariable String empcode) {

        return service.getApplicationStatus(empcode);


    }

    @GetMapping("/status-all")
public ResponseEntity<?> getAllStatuses() {
    return ResponseEntity.ok(gpfWithdrawlService.getAllApplicationStatus());
}

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



   @GetMapping("/employee/{empcode}")
    public ResponseEntity<?> getByEmpcode(@PathVariable String empcode) {
        try {
            return ResponseEntity.ok(gpfWithdrawlService.getByEmpcode(empcode));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    //@Autowired
    //private GpfWithdrawlService gpfWithdrawlService;

    @Autowired
    private ActionMasterRepository actionRepo;

    @PostMapping("/process")
    public ResponseEntity<?> processWorkflow(
            @RequestBody WorkflowProcessRequestDTO request) {

        try {
            gpfWithdrawlService.processApplications(request);
            return ResponseEntity.ok("Applications processed successfully");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/actions")
    public ResponseEntity<?> getAllActions() {
        return ResponseEntity.ok(actionRepo.findAll());
    }

    @GetMapping("/inbox/{roleId}")
public ResponseEntity<?> getInbox(@PathVariable Long roleId) {
    try {
        return ResponseEntity.ok(
                gpfWithdrawlService.getInboxByRole(roleId)
        );
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}





@GetMapping("/trail/{applicationId}")
public ResponseEntity<?> getTrail(@PathVariable Long applicationId) {

    try {

        List<ApplicationTrailDTO> trail =
                gpfWithdrawlService.getTrail(applicationId);

        return ResponseEntity.ok(trail);

    } catch (Exception e) {

        e.printStackTrace();
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}


//@GetMapping("/inbox")
//public ResponseEntity<?> getInboxAuto() {
  //  try {

    //    Long roleId = gpfWithdrawlService.getCurrentWorkflowRole();

      //  return ResponseEntity.ok(
        //        gpfWithdrawlService.getInboxByRole(roleId)
        //);

    //} catch (Exception e) {
     //   return ResponseEntity.badRequest().body(e.getMessage());
    //}
//}

@GetMapping("/inbox")
public ResponseEntity<?> getAllPending() {
    try {

        return ResponseEntity.ok(
                gpfWithdrawlService.getAllPendingApplications()
        );

    } catch (Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}


}


