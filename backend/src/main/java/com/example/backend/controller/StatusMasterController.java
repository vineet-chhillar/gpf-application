package com.example.backend.controller;

import com.example.backend.service.StatusMasterService;

import com.example.backend.entity.StatusMaster;
import com.example.backend.dto.StatusMasterDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/statusmaster")
@CrossOrigin(origins = "http://localhost:3000")
public class StatusMasterController {

    private final StatusMasterService service;

    public StatusMasterController(StatusMasterService service) {
        this.service = service;
    }

    @GetMapping
    public List<StatusMaster> getAll() {
        return service.getAllStatuses();
    }

  @PostMapping
public StatusMaster create(@Valid @RequestBody StatusMasterDto dto) {
    return service.createStatus(dto);
}


    @PutMapping("/{id}")
public StatusMaster update(
        @PathVariable Long id,
        @Valid @RequestBody StatusMasterDto dto) {
    return service.updateStatus(id, dto);
}


    @PatchMapping("/{id}/toggle-active")
    public StatusMaster toggleActive(@PathVariable Long id) {
        return service.toggleActive(id);
    }
}

