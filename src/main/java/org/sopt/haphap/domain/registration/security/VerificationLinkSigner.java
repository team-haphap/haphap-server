package org.sopt.haphap.domain.registration.security;

import org.sopt.haphap.domain.registration.code.RegistrationErrorCode;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Component
public class VerificationLinkSigner {

    private static final Duration TOKEN_TTL = Duration.ofHours(24);
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    @Value("${verification.link.secret}")
    private String secret;

    public String issue(Long registrationId, VerificationDecisionType action) {
        long expiresAt = System.currentTimeMillis() + TOKEN_TTL.toMillis();
        String payload = registrationId + ":" + action.name() + ":" + expiresAt;
        String signature = sign(payload);
        String raw = payload + ":" + signature;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public VerificationDecisionToken verify(String token) {
        String raw;
        try {
            raw = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new CustomException(RegistrationErrorCode.INVALID_VERIFICATION_TOKEN);
        }

        String[] parts = raw.split(":");
        if (parts.length != 4) {
            throw new CustomException(RegistrationErrorCode.INVALID_VERIFICATION_TOKEN);
        }

        String payload = parts[0] + ":" + parts[1] + ":" + parts[2];
        String signature = parts[3];
        if (!sign(payload).equals(signature)) {
            throw new CustomException(RegistrationErrorCode.INVALID_VERIFICATION_TOKEN);
        }

        Long registrationId = Long.parseLong(parts[0]);
        VerificationDecisionType action = VerificationDecisionType.valueOf(parts[1]);
        long expiresAt = Long.parseLong(parts[2]);

        VerificationDecisionToken decoded = new VerificationDecisionToken(registrationId, action, expiresAt);
        if (decoded.isExpired()) {
            throw new CustomException(RegistrationErrorCode.VERIFICATION_TOKEN_EXPIRED);
        }
        return decoded;
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("서명 생성 실패", e);
        }
    }
}

//JWT 서명처럼, HMAC 서명 기반 무상태를 인증한 패턴을 사용했습니다