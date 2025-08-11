package com.bank.kyc.service;

import com.bank.kyc.dto.KycDocumentResponse;
import com.bank.kyc.dto.KycStatsDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface KycService {
    KycDocumentResponse uploadDocument(Long customerId, String name, MultipartFile file);
    List<KycDocumentResponse> getDocumentsByCustomer(Long customerId);
    KycDocumentResponse updateStatus(Long documentId, String status, String message, String verifiedBy);
    void downloadDocument(Long documentId, HttpServletResponse response) throws IOException;
    void deleteDocument(Long documentId, String username);
    List<KycDocumentResponse> getPendingVerifications();
    Map<String, Long> getKycStats();

    // NEW: Document ownership verification
    boolean verifyDocumentOwnership(Long documentId, Long customerId);

    // ✅ NEW: Document verification and rejection
    void verifyDocument(Long documentId, String message, String verifiedBy);
    void rejectDocument(Long documentId, String message, String verifiedBy);

    // ✅ NEW: Check if all required documents for a customer are verified
    boolean areAllDocumentsVerified(Long customerId);

    // ✅ NEW: Get structured KYC statistics
    KycStatsDTO getKYCStatistics();

    // ✅ NEW: Raw documents by customerId (if needed separately)
    List<com.bank.kyc.entity.KycDocument> getDocumentsByCustomerId(Long customerId);
}
