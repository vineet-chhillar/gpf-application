package com.example.backend.controller;
import com.example.backend.dto.ApplicationTrailDTO;
import com.example.backend.dto.GpfApplicationStatusResponseDTO;
import com.example.backend.dto.GpfWithdrawlRequestDTO;
import com.example.backend.dto.WorkflowProcessRequestDTO;

import com.example.backend.repository.ActionMasterRepository;


import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.backend.service.GpfWithdrawlService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;




@RestController
@RequestMapping("/api/gpf-withdrawl")
public class GpfWithdrawlController {

    private final JdbcTemplate jdbcTemplate;

    public GpfWithdrawlController(GpfWithdrawlService gpfWithdrawlService, ActionMasterRepository actionRepo, JdbcTemplate jdbcTemplate) 
    {
        this.gpfWithdrawlService = gpfWithdrawlService;
        this.actionRepo = actionRepo;
       this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/status/{empcode}")
    public GpfApplicationStatusResponseDTO getStatus(@PathVariable String empcode) {
        return gpfWithdrawlService.getApplicationStatus(empcode);
    }

    @GetMapping("/details/{pan}")
    public ResponseEntity<?> getDetails(@PathVariable String pan) {
    try {
        System.out.print("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk");
        return ResponseEntity.ok(gpfWithdrawlService.getDetailsByPan(pan));
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}


@GetMapping("/master/{empcode}")
public ResponseEntity<?> getMasterFromDb(@PathVariable String empcode) {
    try {
            System.out.println("Search Path: " +
    jdbcTemplate.queryForObject("SHOW search_path", String.class));
System.out.print("hggfjhfghgfjgfgjfjgfjghfghfjgfhjgfhjgfjhfjhg");
        String query = """
                select ed.emp_code,
       emp_name empname,
       desg_code, desg_desc designation,
       mobile empmobileno ,email_id empemailid,
       join_nic dateofjoining,
       superannuation_date dateofsuperannuation,
       pan_no panno,
       coalesce(x.functional_roles, '[]'::json) as functional_roles
from public.employee_data ed
         left join (select  emp_code,
                            json_agg(json_build_object(
                                    'post_id', fd.post_id,
                                    'post_name', fp.post_name_en,
                                    'div_code', dm.div_code,
                                    'div_name', dm.div_name
                                )) as functional_roles
                    from public.functional_designation fd
                             left join public.functional_post fp on fd.post_id = fp.post_id and fd.is_active
                             left join public.div_mast dm on fd.div_code = dm.div_code
                    where fd.is_active
                    group by fd.emp_code) x on ed.emp_code = x.emp_code
where emp_status is null and ed.emp_code = ?                   
                """;

                Long empCodeLong = Long.parseLong(empcode);
        List<Map<String, Object>> rows =
                jdbcTemplate.queryForList(query, empCodeLong);

                
        if (rows.isEmpty()) {
            return ResponseEntity.ok(null);
        }

        Map<String, Object> firstRow = rows.get(0);

        Map<String, Object> mapped = new HashMap<>();

        // 🔹 Employee fields (take from first row)
        mapped.put("empcode", firstRow.get("emp_code"));
        mapped.put("empname", firstRow.get("empname"));
        mapped.put("designation", firstRow.get("designation"));

        mapped.put("empmobileno", firstRow.get("empmobileno"));
        mapped.put("empemailid", firstRow.get("empemailid"));

        mapped.put("dateofjoining", firstRow.get("dateofjoining"));
        mapped.put("dateofsuperannuation", firstRow.get("dateofsuperannuation"));

        mapped.put("panno", firstRow.get("panno"));

        Object rolesObj = firstRow.get("functional_roles");

List<Map<String, Object>> roles = new ArrayList<>();

if (rolesObj != null) {
    try {
        ObjectMapper mapper = new ObjectMapper();

        roles = mapper.readValue(
            rolesObj.toString(),
            new TypeReference<List<Map<String, Object>>>() {}
        );

    } catch (Exception e) {
        e.printStackTrace();
    }
}

        // 🔹 First role (same behavior as old API)
        Map<String, Object> firstRole =
                (!roles.isEmpty()) ? roles.get(0) : null;

        mapped.put("empdivision",
                firstRole != null ? firstRole.get("div_name") : null);

        mapped.put("functionalpost",
                firstRole != null ? firstRole.get("post_name") : null);

        // 🔥 Keep full roles list
        mapped.put("roles", roles);

        System.out.print(mapped);
        return ResponseEntity.ok(mapped);

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.badRequest().body("Failed to fetch data from DB");
    }
         
         
}
    @GetMapping("/status-all")
    public ResponseEntity<?> getAllStatuses() 
    {
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


