package com.example.backend.controller;
import com.example.backend.dto.ApplicationTrailDTO;
import com.example.backend.dto.GpfApplicationStatusResponseDTO;
import com.example.backend.dto.GpfWithdrawlRequestDTO;
import com.example.backend.dto.WorkflowProcessRequestDTO;

import com.example.backend.repository.ActionMasterRepository;


import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
@GetMapping("/details/{pan}")
public ResponseEntity<?> getDetails(@PathVariable String pan) {
    try {
        return ResponseEntity.ok(gpfWithdrawlService.getDetailsByPan(pan));
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}

@GetMapping("/master/{empcode}")
public ResponseEntity<?> getMasterFromNic(@PathVariable String empcode) {

    try {

        String url = "https://digital.nic.in/gpf/api/v1/gpf_data.php/?empcode=" + empcode;

        RestTemplate restTemplate = new RestTemplate();

        List<Map<String, Object>> response =
                restTemplate.getForObject(url, List.class);

        if (response == null || response.isEmpty()) {
            return ResponseEntity.ok(null);
        }

        Map<String, Object> data = response.get(0);

        List<Map<String, Object>> roles =
                (List<Map<String, Object>>) data.get("functional_roles");

        Map<String, Object> role =
                (roles != null && !roles.isEmpty()) ? roles.get(0) : null;

        Map<String, Object> mapped = new HashMap<>();

        mapped.put("empcode", data.get("emp_code"));
        mapped.put("empname", data.get("empname"));
        mapped.put("designation", data.get("designation"));

        mapped.put("empdivision", role != null ? role.get("div_name") : null);
        mapped.put("functionalpost", role != null ? role.get("post_id") : null);

        mapped.put("empmobileno", data.get("empmobileno"));
        mapped.put("empemailid", data.get("empemailid"));

        mapped.put("dateofjoining", data.get("dateofjoining"));
        mapped.put("dateofsuperannuation", data.get("dateofsuperannuation"));

        mapped.put("panno", data.get("panno"));

        // 🔥 IMPORTANT: send all roles also
        mapped.put("roles", roles);

        return ResponseEntity.ok(mapped);

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.badRequest().body("Failed to fetch NIC data");
    }
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

            System.out.println("FULL DTO: " + dto);
    System.out.println("MASTER: " + dto.getMaster());

    if (dto.getMaster() != null) {
        System.out.println("EMPCODE IN CONTROLLER: " + dto.getMaster().getEmpcode());
    }

    
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
@PutMapping("/update/{id}")
public ResponseEntity<?> updateWithdrawal(
        @PathVariable Long id,
        @RequestBody GpfWithdrawlRequestDTO dto) {

    try {
        gpfWithdrawlService.updateWithdrawal(id, dto);
        return ResponseEntity.ok("Updated successfully");

    } catch (Exception e) {
        e.printStackTrace();   // 🔥 ADD THIS
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}

}


