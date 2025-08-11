package com.bank.kyc.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = null;

        // Extract token from Authorization header or cookie
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            log.debug("🔍 JWT extracted from Authorization header");
        } else {
            // Optional: Extract token from cookie if you use cookies for JWT
            var cookie = request.getCookies();
            if (cookie != null) {
                for (var c : cookie) {
                    if ("ADMIN_JWT".equals(c.getName())) {
                        token = c.getValue();
                        log.debug("🔍 JWT extracted from cookie ADMIN_JWT");
                        break;
                    }
                }
            }
        }

        if (token != null) {
            if (jwtUtils.validateToken(token)) {
                var user = jwtUtils.extractUserFromToken(token);

                // ✅ FIX: Create authorities from user role
                var authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole())
                );

                var authentication = new UsernamePasswordAuthenticationToken(
                        user, null, authorities // ← Now includes authorities!
                );
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("✅ JWT is valid for user: {} with role: {} and authorities: {}",
                        user.getUsername(), user.getRole(), authorities);
            } else {
                log.warn("❌ Invalid or expired JWT token");
                // Optionally, return 401 and stop filter chain:
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid or expired token\"}");
                return;
            }
        } else {
            log.debug("⚠️ No JWT token found in request");
        }

        filterChain.doFilter(request, response);
    }
}