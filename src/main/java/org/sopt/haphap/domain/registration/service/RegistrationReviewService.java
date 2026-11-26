package org.sopt.haphap.domain.registration.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.service.aggregate.StageResultCountUpdater;
import org.sopt.haphap.domain.registration.code.RegistrationErrorCode;
import org.sopt.haphap.domain.registration.domain.RegistrationReview;
import org.sopt.haphap.domain.registration.domain.RegistrationReviewReason;
import org.sopt.haphap.domain.registration.domain.RegistrationReviewStatus;
import org.sopt.haphap.domain.registration.dto.response.RegistrationReviewResponse;
import org.sopt.haphap.domain.registration.repository.RegistrationReviewRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegistrationReviewService {

    private final RegistrationReviewRepository registrationReviewRepository;
    private final StageResultCountUpdater stageResultCountUpdater;

    public List<RegistrationReviewResponse> getPendingReviews() {
        return registrationReviewRepository.findByStatusOrderByCreatedAtAsc(RegistrationReviewStatus.PENDING)
                .stream()
                .map(RegistrationReviewResponse::from)
                .toList();
    }

    @Transactional
    public RegistrationReviewResponse resolve(Long reviewId, boolean accept) {
        RegistrationReview review = registrationReviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(RegistrationErrorCode.REVIEW_NOT_FOUND));
        if (!review.isPending()) {
            throw new CustomException(RegistrationErrorCode.REVIEW_ALREADY_RESOLVED);
        }

        if (accept) {
            review.accept();
            if (review.getReason() == RegistrationReviewReason.SUBSEQUENT_STAGE_FAIL) {
                stageResultCountUpdater.tryApply(review.getRegistration());
            }
        } else {
            review.reject();
        }

        return RegistrationReviewResponse.from(review);
    }
}
