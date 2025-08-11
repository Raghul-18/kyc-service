package com.bank.kyc.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KycStatsDTO {
    private long total;
    private long pending;
    private long verified;
    private long rejected;
}