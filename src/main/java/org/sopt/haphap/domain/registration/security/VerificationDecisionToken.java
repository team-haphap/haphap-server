package org.sopt.haphap.domain.registration.security;

public record VerificationDecisionToken(Long registrationId, VerificationDecisionType action, long expiresAt) {
    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
}