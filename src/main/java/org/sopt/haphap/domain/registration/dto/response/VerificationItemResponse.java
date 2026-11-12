package org.sopt.haphap.domain.registration.dto.response;

import org.sopt.haphap.domain.registration.domain.Registration;
import org.sopt.haphap.domain.registration.domain.RegistrationVerificationStatus;

import java.time.LocalDateTime;

public record VerificationItemResponse(
        Long registrationId,
        String companyName,
        String companyLogoImageUrl,
        String postingTitle,
        String stageName,
        RegistrationVerificationStatus status,
        LocalDateTime submittedAt,
        LocalDateTime resolvedAt,
        boolean resubmittable
) {
    public static VerificationItemResponse from(Registration r) {
        return new VerificationItemResponse(
                r.getId(),
                r.getPosting().getCompany().getName(),
                r.getPosting().getCompany().getCardLogoImageUrl(),
                r.getPosting().getTitle(),
                r.getStage().getName(),
                r.getVerificationStatus(),
                r.getVerificationSubmittedAt(),
                r.getVerificationResolvedAt(),
                r.isVerificationResubmittable());
    }
}