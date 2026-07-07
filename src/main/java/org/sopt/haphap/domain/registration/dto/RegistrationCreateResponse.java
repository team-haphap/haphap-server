package org.sopt.haphap.domain.registration.dto;

import org.sopt.haphap.domain.registration.domain.Registration;

public record RegistrationCreateResponse(
        Long registrationId,
        PassCardResponse card    // PASS가 아니면 null
) {
    public static RegistrationCreateResponse from(Registration registration) {
        PassCardResponse card = registration.isPass()
                ? PassCardResponse.from(registration)
                : null;
        return new RegistrationCreateResponse(registration.getId(), card);
    }
}