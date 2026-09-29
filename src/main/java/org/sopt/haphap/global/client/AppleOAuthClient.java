package org.sopt.haphap.global.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.user.entity.Provider;
import org.sopt.haphap.global.client.dto.AppleJwksResponse;
import org.sopt.haphap.global.client.dto.AppleTokenExchangeResult;
import org.sopt.haphap.global.client.dto.AppleTokenResponse;
import org.sopt.haphap.global.client.dto.OAuthUserInfo;
import org.sopt.haphap.global.code.AuthErrorCode;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppleOAuthClient implements OAuthClient {

    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final String APPLE_JWKS_URI = "https://appleid.apple.com/auth/keys";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AppleClientSecretGenerator clientSecretGenerator;

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

    public AppleTokenExchangeResult exchangeAuthorizationCode(String authorizationCode) {
        AppleTokenResponse response = webClient.post()
                .uri("https://appleid.apple.com/auth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("client_id", appleClientId)
                        .with("client_secret", clientSecretGenerator.generate())
                        .with("code", authorizationCode)
                        .with("grant_type", "authorization_code"))
                .retrieve()
                // 4xx(invalid_grant 등: 만료·재사용된 인가 코드) → 클라이언트 토큰 문제
                .onStatus(HttpStatusCode::is4xxClientError,
                        r -> logAndError(r, "애플 토큰 교환 4xx", AuthErrorCode.APPLE_INVALID_TOKEN))
                // 5xx → 애플 서버 장애 (클라이언트가 재시도하면 되는 상황)
                .onStatus(HttpStatusCode::is5xxServerError,
                        r -> logAndError(r, "애플 토큰 교환 5xx", AuthErrorCode.APPLE_SERVER_UNAVAILABLE))
                .bodyToMono(AppleTokenResponse.class)
                .timeout(REQUEST_TIMEOUT)
                .onErrorMap(ex -> !(ex instanceof CustomException), ex -> {
                    log.error("애플 토큰 교환 호출 실패", ex);
                    return new CustomException(AuthErrorCode.APPLE_SERVER_UNAVAILABLE);
                })
                .block();

        if (response == null || response.refreshToken() == null || response.idToken() == null) {
            throw new CustomException(AuthErrorCode.APPLE_INVALID_TOKEN);
        }
        return new AppleTokenExchangeResult(response.refreshToken(), response.idToken());
    }

    public void revoke(String appleRefreshToken) {
        webClient.post()
                .uri("https://appleid.apple.com/auth/revoke")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("client_id", appleClientId)
                        .with("client_secret", clientSecretGenerator.generate())
                        .with("token", appleRefreshToken)
                        .with("token_type_hint", "refresh_token"))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        r -> logAndError(r, "애플 revoke 4xx(요청/자격 문제)", AuthErrorCode.APPLE_SERVER_UNAVAILABLE))
                .onStatus(HttpStatusCode::is5xxServerError,
                        r -> logAndError(r, "애플 revoke 5xx(애플 서버 장애)", AuthErrorCode.APPLE_SERVER_UNAVAILABLE))
                .toBodilessEntity()
                .timeout(REQUEST_TIMEOUT)
                .onErrorMap(ex -> !(ex instanceof CustomException), ex -> {
                    log.error("애플 revoke 호출 실패", ex);
                    return new CustomException(AuthErrorCode.APPLE_SERVER_UNAVAILABLE);
                })
                .block();
    }

    private Mono<CustomException> logAndError(ClientResponse response, String message, AuthErrorCode errorCode) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("")
                .map(body -> {
                    log.warn("{} status={}, body={}", message, response.statusCode().value(), body);
                    return new CustomException(errorCode);
                });
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
                .timeout(REQUEST_TIMEOUT)
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