package com.bank.kyc.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtResponse {

    private String token;
    private Long userId;
    private String username;
    private String role;
}