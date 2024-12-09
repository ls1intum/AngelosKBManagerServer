package com.ase.angelos_kb_backend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

import javax.crypto.SecretKey;

@Component
public class JwtUtil {

    private final String SECRET_KEY = "this_is_a_very_secure_and_long_secret_key_32_chars";
    private final SecretKey signingKey = Keys.hmacShaKeyFor(SECRET_KEY.getBytes()); // Generate a SecretKey

    // Generate JWT Token
    public String generateToken(String email, Long orgId, boolean isSystemAdmin) {
        return Jwts.builder()
                .setSubject(email)
                .claim("orgId", orgId) // Include organisation ID in the token
                .claim("isSystemAdmin", isSystemAdmin)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // Generate Refresh Token
    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)) // 7 days
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // Validate JWT Token
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractEmail(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        final Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    // Extract Email from JWT Token
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Extract Organisation ID from JWT Token
    public Long extractOrgId(String token) {
        return extractAllClaims(token).get("orgId", Long.class);
    }

    // Extract isSystemAdmin from JWT Token
    public boolean extractIsSystemAdmin(String token) {
        return extractAllClaims(token).get("isSystemAdmin", Boolean.class);
    }

    // Extract All Claims
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();
    }
}
