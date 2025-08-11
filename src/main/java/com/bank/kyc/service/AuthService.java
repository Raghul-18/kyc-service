package com.bank.kyc.service;

import com.bank.kyc.dto.*;

public interface AuthService {

    JwtResponse loginAdmin(AdminLoginRequest request);

    JwtResponse refreshToken(String oldToken);
}