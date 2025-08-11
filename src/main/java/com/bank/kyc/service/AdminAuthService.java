package com.bank.kyc.service;

import com.bank.kyc.dto.AdminLoginResponse;
import com.bank.kyc.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

    @Autowired
    private JWTUtil jwtUtil;

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    public AdminLoginResponse authenticate(String username, String password) {
        // Simple authentication for default admin user
        if (!DEFAULT_ADMIN_USERNAME.equals(username) || !DEFAULT_ADMIN_PASSWORD.equals(password)) {
            throw new RuntimeException("Invalid credentials");
        }

        // Generate JWT token for admin
        String token = jwtUtil.generateToken(username, "ADMIN", 999L); // Admin user ID = 999

        return AdminLoginResponse.builder()
                .token(token)
                .username(username)
                .role("ADMIN")
                .userId(999L)
                .build();
    }

    public boolean validateToken(String token) {
        try {
            return jwtUtil.validateToken(token);
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            return jwtUtil.getUsernameFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }

    public String getRoleFromToken(String token) {
        try {
            return jwtUtil.getRoleFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }
}