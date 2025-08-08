package com.bank.kyc.entity;

import com.bank.kyc.util.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "KYC_DOCUMENTS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private String documentName;

    @Column(nullable = false)
    private String documentType;

    @Lob
    @Column(nullable = false)
    private byte[] documentContent;

    @Enumerated(EnumType.STRING)
    private VerificationStatus status;

    @Column(length = 1000)
    private String message;

    private String verifiedBy;

    private LocalDateTime uploadedAt;

    @PrePersist
    public void onCreate() {
        this.uploadedAt = LocalDateTime.now();
        this.status = VerificationStatus.PENDING;
        this.message = "";
        this.verifiedBy = null;
    }
}
