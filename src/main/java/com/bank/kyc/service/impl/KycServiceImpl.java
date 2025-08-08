package com.bank.kyc.service.impl;

import com.bank.kyc.dto.KycDocumentResponse;
import com.bank.kyc.entity.KycDocument;
import com.bank.kyc.repository.KycDocumentRepository;
import com.bank.kyc.service.KycService;
import com.bank.kyc.util.VerificationStatus;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KycServiceImpl implements KycService {

    private final KycDocumentRepository repository;

    @Override
    public KycDocumentResponse uploadDocument(Long customerId, String name, MultipartFile file) {
        try {
            KycDocument doc = KycDocument.builder()
                    .customerId(customerId)
                    .documentName(name)
                    .documentType(file.getContentType())
                    .documentContent(file.getBytes())
                    .build();
            return toResponse(repository.save(doc));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Override
    public List<KycDocumentResponse> getDocumentsByCustomer(Long customerId) {
        return repository.findByCustomerId(customerId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public KycDocumentResponse updateStatus(Long documentId, String status, String message, String verifiedBy) {
        KycDocument doc = repository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        doc.setStatus(VerificationStatus.valueOf(status.toUpperCase()));
        doc.setMessage(message);
        doc.setVerifiedBy(verifiedBy);
        return toResponse(repository.save(doc));
    }

    @Override
    public void downloadDocument(Long documentId, HttpServletResponse response) throws IOException {
        KycDocument doc = repository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        response.setContentType(doc.getDocumentType());
        response.setHeader("Content-Disposition", "inline; filename=\"" + doc.getDocumentName() + "\"");
        response.getOutputStream().write(doc.getDocumentContent());
        response.getOutputStream().flush();
    }

    @Override
    public void deleteDocument(Long documentId, String username) {
        KycDocument doc = repository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        if (doc.getStatus() != VerificationStatus.PENDING) {
            throw new RuntimeException("Cannot delete document once it is verified or rejected");
        }
        repository.delete(doc);
    }

    @Override
    public List<KycDocumentResponse> getPendingVerifications() {
        return repository.findAll().stream()
                .filter(doc -> doc.getStatus() == VerificationStatus.PENDING)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getKycStats() {
        List<KycDocument> all = repository.findAll();
        return Map.of(
                "total", (long) all.size(),
                "verified", all.stream().filter(d -> d.getStatus() == VerificationStatus.VERIFIED).count(),
                "rejected", all.stream().filter(d -> d.getStatus() == VerificationStatus.REJECTED).count(),
                "pending", all.stream().filter(d -> d.getStatus() == VerificationStatus.PENDING).count()
        );
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
