package com.bank.kyc.service;

import com.bank.kyc.dto.KycStatsDTO;
import com.bank.kyc.enums.DocumentStatus;
import com.bank.kyc.repository.KycDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardService {

    @Autowired
    private KycDocumentRepository kycDocumentRepository;

    public KycStatsDTO getKYCStatistics() {
        long total = kycDocumentRepository.count();
        long pending = kycDocumentRepository.countByStatus(DocumentStatus.PENDING);
        long verified = kycDocumentRepository.countByStatus(DocumentStatus.VERIFIED);
        long rejected = kycDocumentRepository.countByStatus(DocumentStatus.REJECTED);

        return KycStatsDTO.builder()
                .total(total)
                .pending(pending)
                .verified(verified)
                .rejected(rejected)
                .build();
    }

    public long getTotalCustomersWithDocuments() {
        return kycDocumentRepository.countDistinctCustomers();
    }

    public long getPendingVerificationsCount() {
        return kycDocumentRepository.countByStatus(DocumentStatus.PENDING);
    }
}