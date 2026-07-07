package org.sopt.haphap.domain.registration.dto;

import org.sopt.haphap.domain.registration.domain.Registration;

public record RegistrationCreateResponse(
        Long registrationId,
        PassCardResponse card    // PASS가 아니면 null
) {

    // PASS: 연관이 fetch join으로 로딩된 registration을 받아 카드 구성
    public static RegistrationCreateResponse pass(Registration registration) {
        return new RegistrationCreateResponse(
                registration.getId(),
                PassCardResponse.from(registration));
    }

    // 그 외: ID만
    public static RegistrationCreateResponse idOnly(Long registrationId) {
        return new RegistrationCreateResponse(registrationId, null);
    }
}