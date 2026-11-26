package org.sopt.haphap.domain.registration.dto.response;

import org.sopt.haphap.domain.registration.domain.Registration;
import org.sopt.haphap.domain.registration.domain.RegistrationVerificationStatus;

public record RegistrationCreateResponse(
        Long registrationId,
        RegistrationVerificationStatus verificationStatus   // NOT_REQUIRED, PENDING
) {

    // PASS: 승인 대기 상태로 등록됨. 합격카드는 승인 이후 /api/v1/pass-cards 로 조회
    public static RegistrationCreateResponse pass(Registration registration) {
        return new RegistrationCreateResponse(
                registration.getId(),
                registration.getVerificationStatus());
    }

    // PASS 외: 인증 불필요
    public static RegistrationCreateResponse idOnly(Long registrationId) {
        return new RegistrationCreateResponse(
                registrationId,
                RegistrationVerificationStatus.NOT_REQUIRED);
    }
}