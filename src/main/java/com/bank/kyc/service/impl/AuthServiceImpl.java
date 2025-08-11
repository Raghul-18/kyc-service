package com.bank.kyc.service.impl;

import com.bank.kyc.dto.*;
import com.bank.kyc.entity.User;
import com.bank.kyc.exception.CustomAuthException;
import com.bank.kyc.repository.UserRepository;
import com.bank.kyc.security.JwtUtils;
import com.bank.kyc.service.AuthService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @Override
    public JwtResponse loginAdmin(AdminLoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new CustomAuthException("Invalid username or password"));

        System.out.println(">> [DEBUG] Input Password: " + request.getPassword());
        System.out.println(">> [DEBUG] Stored Hash: " + user.getPasswordHash());

        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        System.out.println(">> [DEBUG] Matches: " + matches);

        if (!matches) {
            throw new CustomAuthException("Invalid username or password");
        }

        if (!"ADMIN".equalsIgnoreCase(user.getRole()) || !user.isEnabled()) {
            throw new CustomAuthException("Access denied");
        }

        return JwtResponse.builder()
                .userId(user.getUserId())
                .role(user.getRole())
                .token(jwtUtils.generateToken(user))
                .build();
    }

    @Override
    public JwtResponse refreshToken(String oldToken) {
        if (!jwtUtils.validateToken(oldToken)) {
            throw new CustomAuthException("Invalid token");
        }

        User user = jwtUtils.extractUserFromToken(oldToken);

        return JwtResponse.builder()
                .userId(user.getUserId())
                .role(user.getRole())
                .token(jwtUtils.generateToken(user))
                .build();
    }
}