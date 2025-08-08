package com.bank.kyc.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KycUploadRequest {
    private Long customerId;
    private String documentName;
    private MultipartFile file;
}
