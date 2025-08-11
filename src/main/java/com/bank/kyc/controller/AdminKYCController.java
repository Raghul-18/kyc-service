package com.bank.kyc.controller;

import com.bank.kyc.service.KycService;
import com.bank.kyc.service.CustomerIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/verify-document/{documentId}")
    public ResponseEntity<?> verifyDocument(
            @PathVariable Long documentId,
            MultipartHttpServletRequest request) {

        try {
            String message = request.getParameter("message");
            String adminUsername = getAdminUsernameFromToken(request);

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
            String adminUsername = getAdminUsernameFromToken(request);

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

            customerIntegrationService.updateCustomerKYCStatus(customerId, "VERIFIED");

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

    private String getAdminUsernameFromToken(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null) {
            return "admin"; // TODO: Implement JWT parsing
        }
        return "system";
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
