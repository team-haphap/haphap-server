package org.sopt.haphap.global.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.user.entity.Provider;
import org.sopt.haphap.global.client.dto.AppleJwksResponse;
import org.sopt.haphap.global.client.dto.OAuthUserInfo;
import org.sopt.haphap.global.code.AuthErrorCode;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppleOAuthClient implements OAuthClient {

    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final String APPLE_JWKS_URI = "https://appleid.apple.com/auth/keys";

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${apple.client-id:}")
    private String appleClientId;

    @Override
    public Provider getProvider() {
        return Provider.APPLE;
    }

    @Override
    public OAuthUserInfo getUserInfo(String identityToken) {
        String kid = extractKid(identityToken);
        RSAPublicKey publicKey = fetchApplePublicKey(kid);

        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(identityToken)
                    .getPayload();
        } catch (Exception e) {
            throw new CustomException(AuthErrorCode.APPLE_INVALID_TOKEN);
        }

        if (!APPLE_ISSUER.equals(claims.getIssuer())
                || claims.getAudience() == null
                || !claims.getAudience().contains(appleClientId)) {
            throw new CustomException(AuthErrorCode.APPLE_INVALID_TOKEN);
        }

        String providerId = claims.getSubject();
        String email = claims.get("email", String.class);

        if (providerId == null) {
            throw new CustomException(AuthErrorCode.APPLE_ACCOUNT_NOT_FOUND);
        }

        return new OAuthUserInfo(providerId, null, email, null, null, null, null);
    }

    private String extractKid(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
            Map<?, ?> header = objectMapper.readValue(headerJson, Map.class);
            return (String) header.get("kid");
        } catch (Exception e) {
            throw new CustomException(AuthErrorCode.APPLE_INVALID_TOKEN);
        }
    }

    private RSAPublicKey fetchApplePublicKey(String kid) {
        AppleJwksResponse response = webClient.get()
                .uri(APPLE_JWKS_URI)
                .retrieve()
                .bodyToMono(AppleJwksResponse.class)
                .onErrorMap(ex -> {
                    log.error("Apple JWKS 조회 실패", ex);
                    return new CustomException(AuthErrorCode.APPLE_SERVER_UNAVAILABLE);
                })
                .block();

        if (response == null || response.keys() == null) {
            throw new CustomException(AuthErrorCode.APPLE_SERVER_UNAVAILABLE);
        }

        AppleJwksResponse.AppleJwk jwk = response.keys().stream()
                .filter(k -> kid != null && kid.equals(k.kid()))
                .findFirst()
                .orElseThrow(() -> new CustomException(AuthErrorCode.APPLE_INVALID_TOKEN));

        try {
            BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(jwk.n()));
            BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(jwk.e()));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(new RSAPublicKeySpec(modulus, exponent));
        } catch (Exception e) {
            throw new CustomException(AuthErrorCode.APPLE_INVALID_TOKEN);
        }
    }
}