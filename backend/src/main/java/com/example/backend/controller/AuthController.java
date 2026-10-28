
package com.example.backend.controller;

import com.example.backend.dto.GpfEmployeeDetailsDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentEmployee(
            Authentication authentication,
            HttpServletRequest request) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof String)) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        HttpSession session = request.getSession(false);

        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        Object employeeDetails =
                session.getAttribute("GPF_EMPLOYEE_DETAILS");

        if (!(employeeDetails instanceof GpfEmployeeDetailsDTO)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        GpfEmployeeDetailsDTO employee =
                (GpfEmployeeDetailsDTO) employeeDetails;

        return ResponseEntity.ok(employee);
    }
}
