package org.sopt.haphap.domain.posting.service.aggregate;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.domain.StageResultCount;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.posting.repository.StageResultCountRepository;
import org.sopt.haphap.domain.registration.domain.RegistrationResult;
import org.sopt.haphap.domain.registration.event.RegistrationApprovedEvent;
import org.sopt.haphap.domain.registration.event.RegistrationResultChangedEvent;
import org.sopt.haphap.domain.registration.event.StageResultCountedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

/**
 * 집계 정책("합격 인증 처리 기준"): 불합격/대기는 등록 즉시 집계하지만,
 * 합격은 운영진이 인증샷을 승인하기 전까지 집계에 넣지 않는다. 승인은 {@link RegistrationApprovedEvent}로 반영되고,
 * 반려는 애초에 집계에 더해진 적이 없으니 별도 처리가 필요 없다.
 *
 * 후속 전형 결과를 운영진 검토 완료 전까지 집계 보류하는 규칙은 아직 미구현 — 운영진 검토 큐가
 * 따로 없어서 "검토 완료 시 반영"을 지금 붙일 수 없다. 다음 단계(운영진 검토 알림)에서 같이 처리 예정.
 */
@Component
@RequiredArgsConstructor
public class StageResultCountUpdater {

    private static final int THRESHOLD = 5;

    private final StageResultCountRepository repository;
    private final PostingStageRepository postingStageRepository;

    @EventListener
    public void onCreated(StageResultCountedEvent e) {
        if (e.result() == RegistrationResult.PASS) {
            return;   // 합격 인증 승인 전까지 집계 보류 (onApproved에서 반영)
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
        repository.movePendingToConfirmed(e.postingId(), e.stageId(), e.newResult().name());
        detectAnnouncement(e.postingId(), e.stageId(), e.newResult());
    }

    @EventListener
    public void onApproved(RegistrationApprovedEvent e) {
        incrementOrCreate(e.postingId(), e.stageId(), RegistrationResult.PASS);
        detectAnnouncement(e.postingId(), e.stageId(), RegistrationResult.PASS);
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
