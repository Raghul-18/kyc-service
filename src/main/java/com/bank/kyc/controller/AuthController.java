package com.bank.kyc.controller;

import com.bank.kyc.dto.AdminLoginRequest;
import com.bank.kyc.dto.AdminLoginResponse;
import com.bank.kyc.dto.JwtResponse;
import com.bank.kyc.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController - sets ADMIN_JWT cookie on successful login (HttpOnly).
 * Note: authService.loginAdmin(...) must return JwtResponse (token, userId, role, username).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // adjust origin if you use credentials (see notes)
public class AuthController {

    private final AuthService authService;

    // inject expiration (ms) from application.yml
    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    /**
     * Admin login - sets HttpOnly cookie ADMIN_JWT with the JWT token.
     */
    @PostMapping("/admin-login")
    public ResponseEntity<AdminLoginResponse> loginAdmin(
            @Valid @RequestBody AdminLoginRequest request,
            HttpServletResponse servletResponse) {

        // Authenticate & get token info
        JwtResponse jwtResponse = authService.loginAdmin(request);

        // Build cookie max-age in seconds
        long maxAgeSeconds = Math.max(0L, jwtExpirationMs / 1000L);

        // IMPORTANT: set secure(true) in production (when using HTTPS). For local dev with HTTP use secure(false).
        ResponseCookie cookie = ResponseCookie.from("ADMIN_JWT", jwtResponse.getToken())
                .httpOnly(true)
                .secure(false)   // change to true in production
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Lax") // or "None" + secure for cross-site scenarios over HTTPS
                .build();

        servletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // Prepare response body (do not include token)
        AdminLoginResponse resp = new AdminLoginResponse();
        resp.setUserId(jwtResponse.getUserId());
        resp.setUsername(jwtResponse.getUsername());
        resp.setRole(jwtResponse.getRole());
        resp.setMessage("Login successful");

        return ResponseEntity.ok(resp);
    }

    /**
     * Logout endpoint - clears the ADMIN_JWT cookie.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse servletResponse) {
        ResponseCookie deleteCookie = ResponseCookie.from("ADMIN_JWT", "")
                .httpOnly(true)
                .secure(false) // match secure used when setting cookie
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        servletResponse.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
        return ResponseEntity.noContent().build();
    }
}
