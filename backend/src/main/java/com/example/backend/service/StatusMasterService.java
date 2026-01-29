package com.example.backend.service;
import com.example.backend.entity.StatusMaster;
import com.example.backend.exception.DuplicateResourceException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.StatusMasterRepository;
import org.springframework.stereotype.Service;
import com.example.backend.dto.StatusMasterDto;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StatusMasterService {

    private final StatusMasterRepository repository;

    public StatusMasterService(StatusMasterRepository repository) {
        this.repository = repository;
    }

    public List<StatusMaster> getAllStatuses() {
        return repository.findAll();
    }

    public StatusMaster createStatus(StatusMasterDto dto) {

    repository.findByStatusCode(dto.getStatusCode().toUpperCase())
            .ifPresent(s -> {
                throw new DuplicateResourceException("Status code already exists");
            });

    StatusMaster status = new StatusMaster();
    status.setStatusCode(dto.getStatusCode().toUpperCase());
    status.setFinal(dto.getIsFinal());
    status.setActive(dto.getIsActive());
    status.setCreatedAt(LocalDateTime.now());

    return repository.save(status);
}


   public StatusMaster updateStatus(Long id, StatusMasterDto dto) {

    StatusMaster existing = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Status not found"));

    // statusCode NOT editable
    existing.setFinal(dto.getIsFinal());
    existing.setActive(dto.getIsActive());

    return repository.save(existing);
}


    public StatusMaster toggleActive(Long id) {
        StatusMaster status = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Status not found"));

        status.setActive(!status.isActive());
        return repository.save(status);
    }
}
