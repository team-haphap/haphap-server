package org.sopt.haphap.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    // TODO. 만료 토큰을 바꾸어요~
    private static final long ACCESS_TOKEN_EXPIRY = 1000L * 60 * 60 * 24 * 30;      // 1달
    private static final long REFRESH_TOKEN_EXPIRY = 1000L * 60 * 60 * 24 * 90;     // 3달

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long id) {
        return createAccessToken(id, Role.USER);
    }

    public String createAccessToken(Long id, Role role) {
        return Jwts.builder()
                .subject(String.valueOf(id))
                .claim("type", "access")
                .claim("role", role.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRY))
                .signWith(getSigningKey())
                .compact();
    }

    public String createRefreshToken(Long id) {
        return createRefreshToken(id, Role.USER);
    }

    public String createRefreshToken(Long id, Role role) {
        return Jwts.builder()
                .subject(String.valueOf(id))
                .claim("type", "refresh")
                .claim("role", role.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRY))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    public Role getRole(String token) {
        String role = parseClaims(token).get("role", String.class);
        return role != null ? Role.valueOf(role) : Role.USER;
    }

    public boolean validateAccessToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return "access".equals(claims.get("type", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return "refresh".equals(claims.get("type", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getExpiration(String token) {
        Date expiration = parseClaims(token).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }

    public boolean isExpiredAccessToken(String token) {
        try {
            parseClaims(token);
            return false;
        } catch (ExpiredJwtException e) {
            return "access".equals(e.getClaims().get("type", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getUserIdIgnoringExpiration(String token) {
        try {
            return Long.parseLong(parseClaims(token).getSubject());
        } catch (ExpiredJwtException e) {
            return Long.parseLong(e.getClaims().getSubject());
        }
    }
}