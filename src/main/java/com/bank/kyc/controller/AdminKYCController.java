package com.bank.kyc.controller;

import com.bank.kyc.entity.User;
import com.bank.kyc.service.KycService;
import com.bank.kyc.service.CustomerIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminKYCController {

    @Autowired
    private KycService kycService;

    @Autowired
    private CustomerIntegrationService customerIntegrationService;

    // Helper method to get current authenticated user
    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (var cookie : request.getCookies()) {
            if ("ADMIN_JWT".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    @PostMapping("/verify-document/{documentId}")
    public ResponseEntity<?> verifyDocument(
            @PathVariable Long documentId,
            MultipartHttpServletRequest request) {

        try {
            String message = request.getParameter("message");
            User currentUser = getCurrentUser();
            String adminUsername = currentUser.getUsername();

            kycService.verifyDocument(documentId, message, adminUsername);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Document verified successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/reject-document/{documentId}")
    public ResponseEntity<?> rejectDocument(
            @PathVariable Long documentId,
            MultipartHttpServletRequest request) {

        try {
            String message = request.getParameter("message");
            User currentUser = getCurrentUser();
            String adminUsername = currentUser.getUsername();

            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Rejection message is required"
                ));
            }

            kycService.rejectDocument(documentId, message, adminUsername);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Document rejected successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/approve-customer/{customerId}")
    public ResponseEntity<?> approveCustomer(
            @PathVariable Long customerId,
            HttpServletRequest request) {

        try {
            boolean allVerified = kycService.areAllDocumentsVerified(customerId);

            if (!allVerified) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "All documents must be verified before approving customer"
                ));
            }

            // Extract JWT token from cookie
            String jwtToken = extractJwtFromRequest(request);

            customerIntegrationService.updateCustomerKYCStatus(customerId, "VERIFIED", jwtToken);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Customer KYC approved successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getKYCStatistics() {
        try {
            var stats = kycService.getKYCStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to fetch statistics",
                    "message", e.getMessage()
            ));
        }
    }
}
