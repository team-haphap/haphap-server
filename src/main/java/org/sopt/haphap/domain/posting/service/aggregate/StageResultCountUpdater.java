package org.sopt.haphap.domain.posting.service.aggregate;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.domain.StageResultCount;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.posting.repository.StageResultCountRepository;
import org.sopt.haphap.domain.posting.service.calculator.StagePositionChecker;
import org.sopt.haphap.domain.registration.domain.Registration;
import org.sopt.haphap.domain.registration.domain.RegistrationResult;
import org.sopt.haphap.domain.registration.domain.RegistrationReviewReason;
import org.sopt.haphap.domain.registration.domain.RegistrationReviewStatus;
import org.sopt.haphap.domain.registration.event.RegistrationApprovedEvent;
import org.sopt.haphap.domain.registration.event.RegistrationResultChangedEvent;
import org.sopt.haphap.domain.registration.event.StageResultCountedEvent;
import org.sopt.haphap.domain.registration.repository.RegistrationReviewRepository;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

/**
 * 집계 정책(transfer_new_new.md):
 * - 합격은 운영진이 인증샷을 승인({@link RegistrationApprovedEvent})하기 전까지 집계 보류.
 * - 후속 전형의 불합격은 운영진 검토({@link RegistrationReviewRepository}) 완료 전까지 집계 보류.
 *   검토 승인 시 {@link #tryApply}로 뒤늦게 반영한다.
 */
@Component
@RequiredArgsConstructor
public class StageResultCountUpdater {

    private static final int THRESHOLD = 5;

    private final StageResultCountRepository repository;
    private final PostingStageRepository postingStageRepository;
    private final StagePositionChecker stagePositionChecker;
    private final RegistrationReviewRepository registrationReviewRepository;

    @EventListener
    public void onCreated(StageResultCountedEvent e) {
        if (e.result() == RegistrationResult.PASS) {
            return;   // 합격 인증 승인 전까지 집계 보류 (onApproved에서 반영)
        }
        if (isHeldForReview(e.result(), e.postingId(), e.stageId())) {
            return;   // 후속 전형 불합격 → 운영진 검토 완료 전까지 집계 보류
        }
        incrementOrCreate(e.postingId(), e.stageId(), e.result());
        detectAnnouncement(e.postingId(), e.stageId(), e.result());
    }

    @EventListener
    public void onChanged(RegistrationResultChangedEvent e) {
        // old는 항상 PENDING (PENDING→확정 한 방향)
        if (e.newResult() == RegistrationResult.PASS) {
            repository.decrementPending(e.postingId(), e.stageId());   // pass로는 아직 안 옮김
            return;
        }
        if (isHeldForReview(e.newResult(), e.postingId(), e.stageId())) {
            repository.decrementPending(e.postingId(), e.stageId());   // pending에서는 빼되 fail로는 아직 안 옮김
            return;
        }
        repository.movePendingToConfirmed(e.postingId(), e.stageId(), e.newResult().name());
        detectAnnouncement(e.postingId(), e.stageId(), e.newResult());
    }

    @EventListener
    public void onApproved(RegistrationApprovedEvent e) {
        incrementOrCreate(e.postingId(), e.stageId(), RegistrationResult.PASS);
        detectAnnouncement(e.postingId(), e.stageId(), RegistrationResult.PASS);
    }

    // 운영진이 검토를 승인(ACCEPTED)한 뒤, 보류돼있던 결과를 뒤늦게 집계에 반영
    public void tryApply(Registration registration) {
        if (registration.getResult() != RegistrationResult.FAIL) {
            return;   // 지금은 후속 전형 불합격 보류만 이 경로로 들어옴
        }
        boolean stillBlocked = registrationReviewRepository.existsByRegistrationIdAndReasonAndStatusNot(
                registration.getId(), RegistrationReviewReason.SUBSEQUENT_STAGE_FAIL, RegistrationReviewStatus.ACCEPTED);
        if (stillBlocked) {
            return;
        }
        Long postingId = registration.getPosting().getId();
        Long stageId = registration.getStage().getId();
        incrementOrCreate(postingId, stageId, RegistrationResult.FAIL);
        detectAnnouncement(postingId, stageId, RegistrationResult.FAIL);
    }

    private boolean isHeldForReview(RegistrationResult result, Long postingId, Long stageId) {
        return result == RegistrationResult.FAIL && stagePositionChecker.isSubsequentStage(postingId, stageId);
    }

    private void incrementOrCreate(Long postingId, Long stageId, RegistrationResult result) {
        int updated = repository.increment(postingId, stageId, result.name());
        if (updated == 0) {
            // row가 아직 없음 → 최초 생성. 동시 첫 등록 시 유니크 충돌 가능 → 잡아 재시도
            try {
                repository.save(StageResultCount.init(postingId, stageId, result));
            } catch (DataIntegrityViolationException dup) {
                repository.increment(postingId, stageId, result.name());
            }
        }
    }

    // PASS/FAIL 증가로 방금 임계값을 넘겼으면 발표일 기록
    private void detectAnnouncement(Long postingId, Long stageId, RegistrationResult result) {
        // PENDING은 PASS+FAIL을 안 늘리니 감지 불필요
        if (result == RegistrationResult.PENDING) return;

        Long confirmed = repository.findConfirmedCount(postingId, stageId);
        if (confirmed != null && confirmed >= THRESHOLD) {
            // 발표는 예정일에 났다고 가정 → 예정일로 기록 (입력만 늦을 수 있음)
            postingStageRepository.findById(stageId)
                    .ifPresent(stage -> stage.markAnnouncedIfAbsent(stage.getExpectedAnnouncementDate()));
        }
    }
}
