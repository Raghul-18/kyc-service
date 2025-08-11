package com.bank.kyc.service;

import com.bank.kyc.dto.KycStatsDTO;
import com.bank.kyc.enums.DocumentStatus;
import com.bank.kyc.repository.KycDocumentRepository;
import com.bank.kyc.util.VerificationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AdminDashboardService {

    @Autowired
    private KycDocumentRepository kycDocumentRepository;

    public KycStatsDTO getKYCStatistics() {
        try {
            log.debug("Fetching KYC statistics...");

            long total = kycDocumentRepository.count();
            long pending = kycDocumentRepository.countByStatus(VerificationStatus.PENDING);
            long verified = kycDocumentRepository.countByStatus(VerificationStatus.VERIFIED);
            long rejected = kycDocumentRepository.countByStatus(VerificationStatus.REJECTED);

            log.debug("KYC Stats - Total: {}, Pending: {}, Verified: {}, Rejected: {}",
                    total, pending, verified, rejected);

            return KycStatsDTO.builder()
                    .total(total)
                    .pending(pending)
                    .verified(verified)
                    .rejected(rejected)
                    .build();

        } catch (Exception e) {
            log.error("Error fetching KYC statistics from repository", e);

            // Return default stats instead of null
            return KycStatsDTO.builder()
                    .total(0L)
                    .pending(0L)
                    .verified(0L)
                    .rejected(0L)
                    .build();
        }
    }

    public long getTotalCustomersWithDocuments() {
        try {
            return kycDocumentRepository.countDistinctCustomers();
        } catch (Exception e) {
            log.error("Error fetching total customers count", e);
            return 0L;
        }
    }

    public long getPendingVerificationsCount() {
        try {
            return kycDocumentRepository.countByStatus(VerificationStatus.PENDING);
        } catch (Exception e) {
            log.error("Error fetching pending verifications count", e);
            return 0L;
        }
    }
}