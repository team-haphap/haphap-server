package org.sopt.haphap.domain.registration.dto.response;

import org.sopt.haphap.domain.registration.domain.Registration;
import org.sopt.haphap.domain.registration.domain.RegistrationVerificationStatus;

import java.time.LocalDateTime;
import java.util.List;

public record VerificationItemResponse(
        Long registrationId,
        String companyName,
        String companyLogoImageUrl,
        String postingTitle,
        String stageName,
        RegistrationVerificationStatus status,
        LocalDateTime submittedAt,
        LocalDateTime resolvedAt,
        boolean resubmittable,
        List<String> submittedImageUrls
) {
    public static VerificationItemResponse from(
            Registration registration,
            String squareLogoImageUrl,
            List<String> submittedImageUrls
    ) {
        return new VerificationItemResponse(
                registration.getId(),
                registration.getPosting().getCompany().getName(),
                squareLogoImageUrl,
                registration.getPosting().getTitle(),
                registration.getStage().getName(),
                registration.getVerificationStatus(),
                registration.getVerificationSubmittedAt(),
                registration.getVerificationResolvedAt(),
                registration.isVerificationResubmittable(),
                submittedImageUrls
        );
    }
}