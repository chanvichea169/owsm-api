package com.owsm.AuthService.api;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET must be set to a Base64-encoded key of at least 32 bytes");
        }
        byte[] decodedKey;
        try {
            decodedKey = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT_SECRET must be valid Base64 encoding", exception);
        }
        if (decodedKey.length < 32) {
            throw new IllegalStateException("JWT_SECRET must decode to at least 32 bytes");
        }
        key = Keys.hmacShaKeyFor(decodedKey);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails, null, null);
    }

    public String generateToken(UserDetails userDetails, UUID sessionId, UUID tokenId) {
        Map<String, Object> claims = new HashMap<>();
        if (sessionId != null) {
            claims.put("sid", sessionId.toString());
        }
        var builder = Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration));
        if (tokenId != null) {
            builder.setId(tokenId.toString());
        }
        return builder.signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public UUID extractSessionId(String token) {
        String sessionId = extractClaim(token, claims -> claims.get("sid", String.class));
        return sessionId == null ? null : UUID.fromString(sessionId);
    }

    public UUID extractTokenId(String token) {
        String tokenId = extractClaim(token, Claims::getId);
        return tokenId == null ? null : UUID.fromString(tokenId);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody());
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
