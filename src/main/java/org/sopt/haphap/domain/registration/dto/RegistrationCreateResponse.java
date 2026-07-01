package org.sopt.haphap.domain.registration.dto;

public record RegistrationCreateResponse(
        Long registrationId
) {
    public static RegistrationCreateResponse from(Long id) {
        return new RegistrationCreateResponse(id);
    }
}