package org.sopt.haphap.domain.registration.security;

public record VerificationDecisionToken(Long registrationId, VerificationDecisionType action) {
}