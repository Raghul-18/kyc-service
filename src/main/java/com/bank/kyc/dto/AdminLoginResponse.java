package com.bank.kyc.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminLoginResponse {
    private String token;
    private String username;
    private String role;
    private Long userId;
}