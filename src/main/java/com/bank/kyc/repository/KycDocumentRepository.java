package com.bank.kyc.repository;

import com.bank.kyc.entity.KycDocument;
import com.bank.kyc.enums.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {

    @Query("SELECT COUNT(DISTINCT d.customerId) FROM KycDocument d")
    long countDistinctCustomers();

    long countByStatus(DocumentStatus status);  // <-- Updated

    List<KycDocument> findByCustomerId(Long customerId);

    @Query("SELECT d FROM KycDocument d WHERE d.status = :status ORDER BY d.uploadedAt ASC")
    List<KycDocument> findByStatusOrderByUploadedAtAsc(@Param("status") DocumentStatus status); // <-- Updated
}
