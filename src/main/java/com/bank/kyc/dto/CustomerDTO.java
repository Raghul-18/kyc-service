package com.bank.kyc.dto;

import lombok.Data;

@Data
public class CustomerDTO {
    private Long customerId;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String pan;
    private String aadhaar;
    private String kycStatus; // PENDING, VERIFIED, REJECTED
}