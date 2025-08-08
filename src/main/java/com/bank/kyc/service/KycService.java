package com.bank.kyc.service;

import com.bank.kyc.dto.KycDocumentResponse;
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
}
