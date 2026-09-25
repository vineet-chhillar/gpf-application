package com.example.backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.GenerateOrderRequest;
import com.example.backend.dto.GenerateOrderResponse;
import com.example.backend.service.SanctionOrderService;
import com.example.backend.repository.GpfSanctionOrderRepository;

@RestController
@RequestMapping("/api/sanction-order")
public class SanctionOrderController {

    private final SanctionOrderService sanctionOrderService;
    //private GpfSanctionOrderRepository sanctionOrderRepository;
    private final GpfSanctionOrderRepository sanctionOrderRepository;

    @Autowired
    public SanctionOrderController(SanctionOrderService sanctionOrderService, GpfSanctionOrderRepository sanctionOrderRepository) {
        this.sanctionOrderService = sanctionOrderService;
        this.sanctionOrderRepository = sanctionOrderRepository;
    }

    @PostMapping("/generate-order")
    public ResponseEntity<GenerateOrderResponse> generateOrder(
            @RequestBody GenerateOrderRequest request) {

        System.out.println("Controller called");

        return ResponseEntity.ok(
                sanctionOrderService.generateOrder(request)
        );
    }

    @GetMapping("/view-order/{applicationId}")
    public ResponseEntity<byte[]> viewOrder(@PathVariable Long applicationId) {

        byte[] pdf = sanctionOrderService.getOrderPdf(applicationId);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "inline; filename=sanction-order.pdf")
                .body(pdf);
    }
    @GetMapping("/check-order/{applicationId}")
public ResponseEntity<Map<String, Boolean>> checkOrder(@PathVariable Long applicationId) {

    boolean exists = sanctionOrderRepository
            .findByApplicationId(applicationId)
            .isPresent();

    return ResponseEntity.ok(Map.of("exists", exists));
}
}
