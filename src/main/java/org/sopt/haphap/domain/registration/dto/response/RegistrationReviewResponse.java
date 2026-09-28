package org.sopt.haphap.domain.registration.dto.response;

import java.time.LocalDateTime;
import org.sopt.haphap.domain.registration.domain.RegistrationReview;
import org.sopt.haphap.domain.registration.domain.RegistrationReviewReason;
import org.sopt.haphap.domain.registration.domain.RegistrationReviewStatus;

public record RegistrationReviewResponse(
        Long reviewId, Long registrationId, Long postingId, Long stageId, String stageName,
        RegistrationReviewReason reason, RegistrationReviewStatus status, LocalDateTime createdAt
) {
    public static RegistrationReviewResponse from(RegistrationReview review) {
        var registration = review.getRegistration();
        return new RegistrationReviewResponse(
                review.getId(),
                registration.getId(),
                registration.getPosting().getId(),
                registration.getStage().getId(),
                registration.getStage().getName(),
                review.getReason(),
                review.getStatus(),
                review.getCreatedAt());
    }
}
