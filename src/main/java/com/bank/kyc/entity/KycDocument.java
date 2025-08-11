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


    private LocalDateTime uploadedAt;
    // Add these fields to your existing KYCDocument entity

    @Column(name = "verified_by")
    private String verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "message", length = 1000)
    private String message;

    // Add getters and setters
    public String getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @PrePersist
    public void onCreate() {
        this.uploadedAt = LocalDateTime.now();
        this.status = VerificationStatus.PENDING;
        this.message = "";
        this.verifiedBy = null;
    }
}
