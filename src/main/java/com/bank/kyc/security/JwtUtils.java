package com.bank.kyc.security;

import com.bank.kyc.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    private Key signingKey;

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        log.debug("🔑 JWT signing key initialized (length: {} bytes)", signingKey.getEncoded().length);
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUserId().toString())
                .claim("role", user.getRole())
                .claim("username", user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            if (claims.getExpiration().before(new Date())) {
                log.warn("⏰ Token expired.");
                return false;
            }
            return true;
        } catch (JwtException e) {
            log.warn("🚫 Invalid JWT: {}", e.getMessage());
        } catch (Exception e) {
            log.error("⚠️ Unexpected JWT validation error: {}", e.getMessage(), e);
        }
        return false;
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getUserId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public String getUsername(String token) {
        return getClaims(token).get("username", String.class);
    }

    public User extractUserFromToken(String token) {
        Claims claims = getClaims(token);
        return User.builder()
                .userId(Long.parseLong(claims.getSubject()))
                .role(claims.get("role", String.class))
                .username(claims.get("username", String.class))
                .build();
    }
}
