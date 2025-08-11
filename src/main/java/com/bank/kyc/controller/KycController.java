package com.bank.kyc.controller;

import com.bank.kyc.client.CustomerServiceClient;
import com.bank.kyc.dto.KycDocumentResponse;
import com.bank.kyc.entity.User;
import com.bank.kyc.service.KycService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;
    private final CustomerServiceClient customerServiceClient;

    // Helper method to get current authenticated user
    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    // 🔄 UPDATED: Auto-resolve customerId from JWT
    @PostMapping("/upload")
    public ResponseEntity<KycDocumentResponse> upload(
            @RequestParam String name,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    ) {
        User currentUser = getCurrentUser();
        Long userId = currentUser.getUserId();

        // 🔄 Resolve userId to customerId
        String jwtToken = request.getHeader("Authorization");
        Long customerId = customerServiceClient.getCustomerIdByUserId(userId, jwtToken);

        if (customerId == null) {
            throw new RuntimeException("Customer record not found for user: " + userId);
        }

        log.info("📤 User {} uploading document '{}' for customer {}", userId, name, customerId);
        return ResponseEntity.ok(kycService.uploadDocument(customerId, name, file));
    }

    // 🔄 UPDATED: Auto-resolve customerId for own documents
    @GetMapping("/my-documents")
    public ResponseEntity<List<KycDocumentResponse>> getMyDocuments(HttpServletRequest request) {
        User currentUser = getCurrentUser();
        Long userId = currentUser.getUserId();

        // Resolve userId to customerId
        String jwtToken = request.getHeader("Authorization");
        Long customerId = customerServiceClient.getCustomerIdByUserId(userId, jwtToken);

        if (customerId == null) {
            throw new RuntimeException("Customer record not found for user: " + userId);
        }

        return ResponseEntity.ok(kycService.getDocumentsByCustomer(customerId));
    }

    // 🔄 UPDATED: Access control for document downloads
    @GetMapping("/document/{documentId}/download")
    public void download(@PathVariable Long documentId, HttpServletResponse response, HttpServletRequest request) throws IOException {
        User currentUser = getCurrentUser();

        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            // Admin can download any document
            kycService.downloadDocument(documentId, response);
            return;
        }

        // Customer can only download their own documents
        Long userId = currentUser.getUserId();
        String jwtToken = request.getHeader("Authorization");
        Long customerId = customerServiceClient.getCustomerIdByUserId(userId, jwtToken);

        if (customerId == null) {
            throw new RuntimeException("Customer record not found");
        }

        // Verify document belongs to this customer
        if (!kycService.verifyDocumentOwnership(documentId, customerId)) {
            throw new SecurityException("Access denied - document does not belong to you");
        }

        kycService.downloadDocument(documentId, response);
    }

    @DeleteMapping("/document/{documentId}")
    public ResponseEntity<Void> delete(@PathVariable Long documentId, HttpServletRequest request) {
        User currentUser = getCurrentUser();

        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            kycService.deleteDocument(documentId, currentUser.getUsername());
            return ResponseEntity.noContent().build();
        }

        // Customer can only delete their own documents
        Long userId = currentUser.getUserId();
        String jwtToken = request.getHeader("Authorization");
        Long customerId = customerServiceClient.getCustomerIdByUserId(userId, jwtToken);

        if (customerId == null) {
            throw new RuntimeException("Customer record not found");
        }

        // Verify document belongs to this customer
        if (!kycService.verifyDocumentOwnership(documentId, customerId)) {
            throw new SecurityException("Access denied - document does not belong to you");
        }

        kycService.deleteDocument(documentId, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }

    // ===== ADMIN ENDPOINTS =====

    @GetMapping("/admin/pending-verifications")
    public ResponseEntity<List<KycDocumentResponse>> getPending() {
        validateAdmin();
        return ResponseEntity.ok(kycService.getPendingVerifications());
    }

    @GetMapping("/admin/documents/{customerId}")
    public ResponseEntity<List<KycDocumentResponse>> getCustomerDocuments(@PathVariable Long customerId) {
        validateAdmin();
        return ResponseEntity.ok(kycService.getDocumentsByCustomer(customerId));
    }

    @PutMapping("/admin/verify/{documentId}")
    public ResponseEntity<KycDocumentResponse> verify(
            @PathVariable Long documentId,
            @RequestParam(required = false) String message
    ) {
        validateAdmin();
        User currentUser = getCurrentUser();
        String adminUsername = currentUser.getUsername();

        return ResponseEntity.ok(kycService.updateStatus(
                documentId, "VERIFIED",
                message != null ? message : "Verified by admin",
                adminUsername));
    }

    @PutMapping("/admin/reject/{documentId}")
    public ResponseEntity<KycDocumentResponse> reject(
            @PathVariable Long documentId,
            @RequestParam String message
    ) {
        validateAdmin();
        User currentUser = getCurrentUser();
        String adminUsername = currentUser.getUsername();

        return ResponseEntity.ok(kycService.updateStatus(documentId, "REJECTED", message, adminUsername));
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<?> getStats() {
        validateAdmin();
        return ResponseEntity.ok(kycService.getKycStats());
    }

    // Helper method to validate admin access
    private void validateAdmin() {
        User currentUser = getCurrentUser();
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new SecurityException("Only ADMIN can access this endpoint");
        }
    }
}