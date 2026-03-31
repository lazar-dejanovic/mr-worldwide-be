package com.raf.mrworldwide.services.ums;

import com.raf.mrworldwide.domain.entities.user.User;
import com.raf.mrworldwide.exceptions.SecretNotFoundException;
import com.raf.mrworldwide.security.JwtSecretService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class TokenAuthenticationService {

    static final long EXPIRATION_TIME = 30L * 24 * 60 * 60 * 1000; // 1 month

    // The http authentication header name
    public static final String TOKEN_PREFIX = "Bearer ";

    private final JwtSecretService jwtSecretService;

    public TokenAuthenticationService(JwtSecretService jwtSecretService) {
        this.jwtSecretService = jwtSecretService;
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("role", user.getRole())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey()).build()
                .parseSignedClaims(token.replace(TOKEN_PREFIX, ""))
                .getPayload();
        return claims.getSubject();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecretService.getJwtSigningSecret();
        if (keyBytes == null) {
            throw new SecretNotFoundException("JWT signing secret returned null — cannot build signing key.");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
