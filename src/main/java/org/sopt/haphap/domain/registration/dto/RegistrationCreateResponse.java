package org.sopt.haphap.domain.registration.dto;

public record RegistrationCreateResponse(
        Long registrationId,
        RegistrationStatus status
) {

    public enum RegistrationStatus {
        CREATED,
        UPDATED,
        CONFIRM_REQUIRED
    }

    public static RegistrationCreateResponse created(Long id) {
        return new RegistrationCreateResponse(id, RegistrationStatus.CREATED);
    }
    public static RegistrationCreateResponse updated(Long id) {
        return new RegistrationCreateResponse(id, RegistrationStatus.UPDATED);
    }
    public static RegistrationCreateResponse confirmRequired(Long id) {
        return new RegistrationCreateResponse(id, RegistrationStatus.CONFIRM_REQUIRED);
    }

    public static RegistrationCreateResponse from(Long registrationId, RegistrationStatus status) {
        return new RegistrationCreateResponse(registrationId, status);
    }
}