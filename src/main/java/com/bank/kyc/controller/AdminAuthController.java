package com.bank.kyc.controller;

import com.bank.kyc.dto.AdminLoginRequest;
import com.bank.kyc.dto.AdminLoginResponse;
import com.bank.kyc.service.AdminAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminAuthController {

    @Autowired
    private AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody AdminLoginRequest loginRequest) {
        try {
            AdminLoginResponse response = adminAuthService.authenticate(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid credentials", "message", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> adminLogout(HttpServletRequest request) {
        // For JWT, we don't need server-side logout, just clear client-side token
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null && adminAuthService.validateToken(token)) {
            return ResponseEntity.ok(Map.of("valid", true));
        }
        return ResponseEntity.badRequest().body(Map.of("valid", false));
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}