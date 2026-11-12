package org.sopt.haphap.domain.registration.security;

import org.sopt.haphap.domain.registration.code.RegistrationErrorCode;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class VerificationLinkSigner {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    @Value("${verification.link.secret}")
    private String secret;

    public String issue(Long registrationId, VerificationDecisionType action, int round) {
        String payload = registrationId + ":" + action.name() + ":" + round;
        String raw = payload + ":" + sign(payload);
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
        String payload;
        String signature;
        int round;
        if (parts.length == 3) {
            payload = parts[0] + ":" + parts[1];
            signature = parts[2];
            round = 1;
        } else if (parts.length == 4) {
            payload = parts[0] + ":" + parts[1] + ":" + parts[2];
            signature = parts[3];
            round = parseRound(parts[2]);
        } else {
            throw new CustomException(RegistrationErrorCode.INVALID_VERIFICATION_TOKEN);
        }

        if (!MessageDigest.isEqual(
                sign(payload).getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8))) {
            throw new CustomException(RegistrationErrorCode.INVALID_VERIFICATION_TOKEN);
        }
        return new VerificationDecisionToken(
                Long.parseLong(parts[0]), VerificationDecisionType.valueOf(parts[1]), round);
    }

    private int parseRound(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new CustomException(RegistrationErrorCode.INVALID_VERIFICATION_TOKEN);
        }
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