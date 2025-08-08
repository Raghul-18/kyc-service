package com.bank.kyc.dto;

import com.bank.kyc.util.VerificationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycDocumentResponse {
    private Long documentId;
    private Long customerId;
    private String documentName;
    private String documentType;
    private VerificationStatus status;
    private String message;
    private String verifiedBy;
    private LocalDateTime uploadedAt;
}
