package com.bank.kyc.repository;

import com.bank.kyc.entity.KycDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {
    List<KycDocument> findByCustomerId(Long customerId);
}
