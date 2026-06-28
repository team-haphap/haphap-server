package org.sopt.haphap.registration.event;

public record RegistrationCreatedEvent(
        Long registrationId,
        Long postingId,
        String stage,
        Long registrantUserId
) {
}