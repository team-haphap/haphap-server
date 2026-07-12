package org.sopt.haphap.domain.registration.event;

import org.sopt.haphap.domain.registration.domain.RegistrationResult;

public record RegistrationCreatedEvent(
        Long registrationId,
        Long postingId,
        String stage,
        RegistrationResult result,
        Long registrantUserId
) {
}