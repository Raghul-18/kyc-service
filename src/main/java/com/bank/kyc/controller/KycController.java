package com.bank.kyc.controller;

import com.bank.kyc.dto.KycDocumentResponse;
import com.bank.kyc.service.KycService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;

    // Document Management

    @PostMapping("/upload")
    public ResponseEntity<KycDocumentResponse> upload(
            @RequestParam Long customerId,
            @RequestParam String name,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(kycService.uploadDocument(customerId, name, file));
    }

    @GetMapping("/documents/{customerId}")
    public ResponseEntity<List<KycDocumentResponse>> getAll(@PathVariable Long customerId) {
        return ResponseEntity.ok(kycService.getDocumentsByCustomer(customerId));
    }

    @GetMapping("/document/{documentId}/download")
    public void download(@PathVariable Long documentId, HttpServletResponse response) throws IOException {
        kycService.downloadDocument(documentId, response);
    }

    @DeleteMapping("/document/{documentId}")
    public ResponseEntity<Void> delete(@PathVariable Long documentId, Principal principal) {
        kycService.deleteDocument(documentId, principal.getName());
        return ResponseEntity.noContent().build();
    }

    // Admin Verification

    @GetMapping("/admin/pending-verifications")
    public ResponseEntity<List<KycDocumentResponse>> getPending() {
        return ResponseEntity.ok(kycService.getPendingVerifications());
    }

    @PutMapping("/admin/verify/{documentId}")
    public ResponseEntity<KycDocumentResponse> verify(
            @PathVariable Long documentId,
            @RequestParam(required = false) String message,
            Principal principal
    ) {
        return ResponseEntity.ok(kycService.updateStatus(documentId, "VERIFIED", message != null ? message : "Verified", principal.getName()));
    }

    @PutMapping("/admin/reject/{documentId}")
    public ResponseEntity<KycDocumentResponse> reject(
            @PathVariable Long documentId,
            @RequestParam String message,
            Principal principal
    ) {
        return ResponseEntity.ok(kycService.updateStatus(documentId, "REJECTED", message, principal.getName()));
    }

    @GetMapping("/admin/customer/{customerId}/status")
    public ResponseEntity<List<KycDocumentResponse>> getCustomerStatus(@PathVariable Long customerId) {
        return ResponseEntity.ok(kycService.getDocumentsByCustomer(customerId));
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(kycService.getKycStats());
    }
}
