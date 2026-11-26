package org.sopt.haphap.domain.registration.dto.response;

import org.sopt.haphap.domain.registration.domain.Registration;
import org.sopt.haphap.domain.registration.domain.RegistrationVerificationStatus;

public record VerificationResubmitResponse(Long registrationId, RegistrationVerificationStatus verificationStatus) {

    public static VerificationResubmitResponse from(Registration registration) {
        return new VerificationResubmitResponse(registration.getId(), registration.getVerificationStatus());
    }
}