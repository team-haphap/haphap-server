package org.sopt.haphap.domain.registration.event;

public record VerificationResubmittedEvent(Long registrationId, int round) {
}