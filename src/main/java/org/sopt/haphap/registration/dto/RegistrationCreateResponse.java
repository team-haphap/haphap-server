package org.sopt.haphap.registration.dto;

public record RegistrationCreateResponse(Long registrationId) {
    public static RegistrationCreateResponse from(Long registrationId) {
        return new RegistrationCreateResponse(registrationId);
    }
}