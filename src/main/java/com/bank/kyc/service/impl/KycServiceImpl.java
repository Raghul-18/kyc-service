package com.bank.kyc.service.impl;

import com.bank.kyc.dto.KycDocumentResponse;
import com.bank.kyc.dto.KycStatsDTO;
import com.bank.kyc.entity.KycDocument;
import com.bank.kyc.repository.KycDocumentRepository;
import com.bank.kyc.service.KycService;
import com.bank.kyc.util.VerificationStatus;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.bank.kyc.enums.DocumentStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class KycServiceImpl implements KycService {

    private final KycDocumentRepository repository;

    @Override
    public KycDocumentResponse uploadDocument(Long customerId, String name, MultipartFile file) {
        try {
            log.info("📤 Uploading document '{}' for customer {}", name, customerId);

            KycDocument doc = KycDocument.builder()
                    .customerId(customerId)
                    .documentName(name)
                    .documentType(file.getContentType())
                    .documentContent(file.getBytes())
                    .build();

            KycDocument saved = repository.save(doc);
            log.info("✅ Document uploaded successfully with ID {}", saved.getDocumentId());

            return toResponse(saved);
        } catch (IOException e) {
            log.error("❌ Failed to upload file: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Override
    public List<KycDocumentResponse> getDocumentsByCustomer(Long customerId) {
        log.info("🔍 Fetching documents for customer {}", customerId);

        List<KycDocument> documents = repository.findByCustomerId(customerId);
        log.info("📄 Found {} documents for customer {}", documents.size(), customerId);

        return documents.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public KycDocumentResponse updateStatus(Long documentId, String status, String message, String verifiedBy) {
        log.info("🔄 Updating document {} status to {} by {}", documentId, status, verifiedBy);

        KycDocument doc = repository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        doc.setStatus(VerificationStatus.valueOf(status.toUpperCase()));
        doc.setMessage(message);
        doc.setVerifiedBy(verifiedBy);
        doc.setVerifiedAt(LocalDateTime.now());

        KycDocument updated = repository.save(doc);
        log.info("✅ Document {} status updated to {}", documentId, status);

        return toResponse(updated);
    }

    @Override
    public void downloadDocument(Long documentId, HttpServletResponse response) throws IOException {
        log.info("⬇️ Downloading document {}", documentId);

        KycDocument doc = repository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        response.setContentType(doc.getDocumentType());
        response.setHeader("Content-Disposition", "inline; filename=\"" + doc.getDocumentName() + "\"");
        response.getOutputStream().write(doc.getDocumentContent());
        response.getOutputStream().flush();

        log.info("✅ Document {} downloaded successfully", documentId);
    }

    @Override
    public void deleteDocument(Long documentId, String username) {
        log.info("🗑️ Deleting document {} by {}", documentId, username);

        KycDocument doc = repository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (doc.getStatus() != VerificationStatus.PENDING) {
            throw new RuntimeException("Cannot delete document once it is verified or rejected");
        }

        repository.delete(doc);
        log.info("✅ Document {} deleted by {}", documentId, username);
    }

    @Override
    public List<KycDocumentResponse> getPendingVerifications() {
        log.info("🔍 Fetching pending verifications");

        List<KycDocument> pending = repository.findAll().stream()
                .filter(doc -> doc.getStatus() == VerificationStatus.PENDING)
                .collect(Collectors.toList());

        log.info("📄 Found {} pending verifications", pending.size());

        return pending.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getKycStats() {
        log.info("📊 Generating KYC statistics");

        List<KycDocument> all = repository.findAll();
        Map<String, Long> stats = Map.of(
                "total", (long) all.size(),
                "verified", all.stream().filter(d -> d.getStatus() == VerificationStatus.VERIFIED).count(),
                "rejected", all.stream().filter(d -> d.getStatus() == VerificationStatus.REJECTED).count(),
                "pending", all.stream().filter(d -> d.getStatus() == VerificationStatus.PENDING).count()
        );

        log.info("📊 KYC Stats: {}", stats);
        return stats;
    }

    @Override
    public boolean verifyDocumentOwnership(Long documentId, Long customerId) {
        log.debug("🔐 Verifying document {} belongs to customer {}", documentId, customerId);

        return repository.findById(documentId)
                .map(doc -> doc.getCustomerId().equals(customerId))
                .orElse(false);
    }

    @Override
    public void verifyDocument(Long documentId, String message, String verifiedBy) {
        log.info("✅ Verifying document {} by {}", documentId, verifiedBy);

        KycDocument document = repository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        document.setStatus(VerificationStatus.VERIFIED);
        document.setMessage(message != null ? message : "Document verified successfully");
        document.setVerifiedBy(verifiedBy);
        document.setVerifiedAt(LocalDateTime.now());

        repository.save(document);
        log.info("✅ Document {} verified successfully", documentId);
    }

    @Override
    public void rejectDocument(Long documentId, String message, String verifiedBy) {
        log.info("❌ Rejecting document {} by {}", documentId, verifiedBy);

        if (message == null || message.trim().isEmpty()) {
            throw new RuntimeException("Rejection message is required");
        }

        KycDocument document = repository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        document.setStatus(VerificationStatus.REJECTED);
        document.setMessage(message);
        document.setVerifiedBy(verifiedBy);
        document.setVerifiedAt(LocalDateTime.now());

        repository.save(document);
        log.info("❌ Document {} rejected successfully", documentId);
    }

    @Override
    public boolean areAllDocumentsVerified(Long customerId) {
        log.info("🔍 Checking if all required documents are verified for customer {}", customerId);

        List<KycDocument> documents = getDocumentsByCustomerId(customerId);

        Set<String> requiredTypes = Set.of("AADHAR", "PAN", "PHOTO");
        Set<String> verifiedTypes = documents.stream()
                .filter(doc -> doc.getStatus() == VerificationStatus.VERIFIED)
                .map(doc -> doc.getDocumentType().toUpperCase())
                .collect(Collectors.toSet());

        boolean result = verifiedTypes.containsAll(requiredTypes);
        log.info("✅ All documents verified: {}", result);
        return result;
    }

    @Override
    public KycStatsDTO getKYCStatistics() {
        log.info("📊 Fetching structured KYC statistics");

        long total = repository.count();
        long pending = repository.countByStatus(DocumentStatus.PENDING);
        long verified = repository.countByStatus(DocumentStatus.VERIFIED);
        long rejected = repository.countByStatus(DocumentStatus.REJECTED);

        KycStatsDTO stats = KycStatsDTO.builder()
                .total(total)
                .pending(pending)
                .verified(verified)
                .rejected(rejected)
                .build();

        log.info("📊 Stats: {}", stats);
        return stats;
    }

    @Override
    public List<KycDocument> getDocumentsByCustomerId(Long customerId) {
        log.info("📄 Fetching raw documents for customer {}", customerId);
        return repository.findByCustomerId(customerId);
    }

    private KycDocumentResponse toResponse(KycDocument doc) {
        return KycDocumentResponse.builder()
                .documentId(doc.getDocumentId())
                .customerId(doc.getCustomerId())
                .documentName(doc.getDocumentName())
                .documentType(doc.getDocumentType())
                .uploadedAt(doc.getUploadedAt())
                .status(doc.getStatus())
                .message(doc.getMessage())
                .verifiedBy(doc.getVerifiedBy())
                .build();
    }
}
