package com.bank.kyc.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    private static final String USER_SECURITY_SCHEME = "User Authentication";
    private static final String ADMIN_SECURITY_SCHEME = "Admin Authentication";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("KYC Service API")
                        .description("Know Your Customer (KYC) Document Management API for Banking System")
                        .version("1.0.0"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server")
                ))
                // Require both schemes (you can adjust per controller using @SecurityRequirement)
                .addSecurityItem(new SecurityRequirement().addList(USER_SECURITY_SCHEME))
                .addSecurityItem(new SecurityRequirement().addList(ADMIN_SECURITY_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(USER_SECURITY_SCHEME, createBearerScheme("JWT token for normal user APIs"))
                        .addSecuritySchemes(ADMIN_SECURITY_SCHEME, createBearerScheme("JWT token for admin APIs")));
    }

    private SecurityScheme createBearerScheme(String description) {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description(description);
    }
}
