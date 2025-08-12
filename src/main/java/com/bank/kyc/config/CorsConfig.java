package com.bank.kyc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ Allowed origins - you can add more if needed
        configuration.setAllowedOrigins(List.of("http://localhost:8080","http://localhost:8081","http://localhost:8084","http://localhost:8083")); // Frontend or Gateway

        // ✅ Allow all methods (GET, POST, PUT, DELETE, OPTIONS, etc.)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // ✅ Allow all headers
        configuration.setAllowedHeaders(List.of("*"));

        // ✅ Allow credentials (cookies, authorization headers, etc.)
        configuration.setAllowCredentials(true);

        // Optional: Set max age for preflight cache (in seconds)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Covers all endpoints
        return source;
    }
}
