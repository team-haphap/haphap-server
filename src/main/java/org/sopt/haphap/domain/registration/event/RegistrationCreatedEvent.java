package org.sopt.haphap.domain.registration.event;

public record RegistrationCreatedEvent(
        Long registrationId,
        Long postingId,
        String stage,
        Long registrantUserId
) {
}