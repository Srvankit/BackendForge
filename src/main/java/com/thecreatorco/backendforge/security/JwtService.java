package com.thecreatorco.backendforge.security;

import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.thecreatorco.backendforge.user.UserEntity;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String EMAIL_CLAIM = "email";
    private static final String TOKEN_USE_CLAIM = "token_use";
    private static final String ACCESS_TOKEN_USE = "access";
    private static final String TOKEN_TYPE = "Bearer";

    private final SecretKey signingKey;
    private final long expirationSeconds;
    private final String issuer;
    private final String audience;

    public JwtService(
            @Value("${jwt.secret:}") String encodedSecret,
            @Value("${jwt.expiration-seconds:900}") long expirationSeconds,
            @Value("${jwt.issuer:}") String issuer,
            @Value("${jwt.audience:}") String audience) {
        this.signingKey = createSigningKey(encodedSecret);
        if (expirationSeconds <= 0) {
            throw new IllegalStateException("JWT_EXPIRATION_SECONDS must be greater than zero");
        }
        this.issuer = requireConfiguration("JWT_ISSUER", issuer);
        this.audience = requireConfiguration("JWT_AUDIENCE", audience);
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(UserEntity user) {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + expirationSeconds * 1000);
        return Jwts.builder()
                .issuer(issuer)
                .audience().add(audience).and()
                .subject(user.getId().toString())
                .claim(EMAIL_CLAIM, user.getEmail())
                .claim(TOKEN_USE_CLAIM, ACCESS_TOKEN_USE)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public String getTokenType() {
        return TOKEN_TYPE;
    }

    public Claims parseAndValidate(String token) {
        Jws<Claims> parsed = Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(issuer)
                .requireAudience(audience)
                .require(TOKEN_USE_CLAIM, ACCESS_TOKEN_USE)
                .build()
                .parseSignedClaims(token);
        Claims claims = parsed.getPayload();
        if (claims.getSubject() == null || claims.getSubject().isBlank()
                || claims.get(EMAIL_CLAIM, String.class) == null
                || claims.get(EMAIL_CLAIM, String.class).isBlank()) {
            throw new JwtException("Required JWT claims are missing");
        }
        return claims;
    }

    private String requireConfiguration(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " must be configured");
        }
        return value;
    }

    private SecretKey createSigningKey(String encodedSecret) {
        if (encodedSecret == null || encodedSecret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET must be configured with a Base64-encoded secret");
        }

        final byte[] decodedSecret;
        try {
            decodedSecret = Base64.getDecoder().decode(encodedSecret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT_SECRET must be valid Base64", exception);
        }

        if (decodedSecret.length < 32) {
            throw new IllegalStateException("JWT_SECRET must decode to at least 32 bytes");
        }
        return new SecretKeySpec(decodedSecret, "HmacSHA256");
    }
}
