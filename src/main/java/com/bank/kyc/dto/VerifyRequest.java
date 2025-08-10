package com.bank.kyc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyRequest {
    private String status;   // "VERIFIED" or "REJECTED"
    private String remarks;  // optional message
}
