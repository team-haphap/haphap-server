package org.sopt.haphap.domain.registration.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.service.calculator.StagePositionChecker;
import org.sopt.haphap.domain.registration.domain.Registration;
import org.sopt.haphap.domain.registration.domain.RegistrationResult;
import org.sopt.haphap.domain.registration.domain.RegistrationReview;
import org.sopt.haphap.domain.registration.domain.RegistrationReviewReason;
import org.sopt.haphap.domain.registration.domain.RegistrationReviewStatus;
import org.sopt.haphap.domain.registration.event.RegistrationCreatedEvent;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.sopt.haphap.domain.registration.repository.RegistrationReviewRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 운영진 검토 대상 자동 감지. 합격(PASS)은 인증샷을 운영진이 항상 수동으로 승인하기로 하여
 * 그 과정에서 운영진이 직접 판단하므로 대상이 아니다. 불합격(FAIL)은 별도 인증 절차가 없어
 * 운영진이 볼 계기가 없는 두 가지만 감지한다.
 */
@Component
@RequiredArgsConstructor
public class RegistrationReviewDetector {

    private static final int REPEATED_FAILURE_THRESHOLD = 2;

    private final RegistrationRepository registrationRepository;
    private final RegistrationReviewRepository registrationReviewRepository;
    private final StagePositionChecker stagePositionChecker;

    @EventListener
    public void onRegistrationCreated(RegistrationCreatedEvent e) {
        if (e.result() != RegistrationResult.FAIL) {
            return;
        }
        Registration registration = registrationRepository.findById(e.registrationId())
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 등록입니다: " + e.registrationId()));
        Long stageId = registration.getStage().getId();

        detectSubsequentStageFail(registration, e.postingId(), stageId);
        detectRepeatedFailure(registration, e.postingId(), stageId);
    }

    private void detectSubsequentStageFail(Registration registration, Long postingId, Long stageId) {
        if (!stagePositionChecker.isSubsequentStage(postingId, stageId)) {
            return;
        }
        createIfAbsent(registration, RegistrationReviewReason.SUBSEQUENT_STAGE_FAIL);
    }

    private void detectRepeatedFailure(Registration registration, Long postingId, Long stageId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime startOfTomorrow = startOfDay.plusDays(1);

        long failToday = registrationRepository.countFailToday(postingId, stageId, startOfDay, startOfTomorrow);
        if (failToday != REPEATED_FAILURE_THRESHOLD) {
            return;   // 정확히 임계값이 된 순간에만 (그 이후 매번 다시 알리지 않으려고)
        }
        if (registrationRepository.existsApprovedPass(postingId, stageId)) {
            return;   // 이미 승인된 합격이 있으면 대상 아님
        }
        createIfAbsent(registration, RegistrationReviewReason.REPEATED_FAILURE);
    }

    private void createIfAbsent(Registration registration, RegistrationReviewReason reason) {
        boolean exists = registrationReviewRepository.existsByRegistrationIdAndReasonAndStatus(
                registration.getId(), reason, RegistrationReviewStatus.PENDING);
        if (exists) {
            return;
        }
        registrationReviewRepository.save(RegistrationReview.create(registration, reason));
    }
}
