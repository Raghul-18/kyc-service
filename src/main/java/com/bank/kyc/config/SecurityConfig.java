package com.bank.kyc.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("🔧 Configuring SecurityFilterChain without JwtAuthenticationFilter");

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/kyc/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/kyc/**").hasAnyRole("CUSTOMER", "ADMIN")
                        .anyRequest().authenticated()
                );

        log.info("✅ SecurityFilterChain configured successfully");
        return http.build();
    }
}
