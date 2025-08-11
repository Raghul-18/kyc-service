package com.bank.kyc.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String username;  // For admin: actual username, for customer: customer_<aadhaar>

    @Column(nullable = false)
    private String role; // ADMIN, CUSTOMER, VERIFIER

    @Column
    private String passwordHash; // Only used for admins

    @Column(name = "enabled", nullable = false, columnDefinition = "NUMBER(1)")
    private boolean enabled = true;

    @Column
    private LocalDateTime createdAt = LocalDateTime.now();
}