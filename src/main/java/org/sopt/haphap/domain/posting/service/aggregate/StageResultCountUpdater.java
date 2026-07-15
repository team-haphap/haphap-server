package org.sopt.haphap.domain.posting.service.aggregate;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.domain.StageResultCount;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.posting.repository.StageResultCountRepository;
import org.sopt.haphap.domain.registration.domain.RegistrationResult;
import org.sopt.haphap.domain.registration.event.RegistrationResultChangedEvent;
import org.sopt.haphap.domain.registration.event.StageResultCountedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class StageResultCountUpdater {

    private static final int THRESHOLD = 5;

    private final StageResultCountRepository repository;
    private final PostingStageRepository postingStageRepository;

    @EventListener
    public void onCreated(StageResultCountedEvent e) {
        int updated = repository.increment(e.postingId(), e.stageId(), e.result().name());
        if (updated == 0) {
            // row가 아직 없음 → 최초 생성. 동시 첫 등록 시 유니크 충돌 가능 → 잡아 재시도
            try {
                repository.save(StageResultCount.init(e.postingId(), e.stageId(), e.result()));
            } catch (DataIntegrityViolationException dup) {
                repository.increment(e.postingId(), e.stageId(), e.result().name());
            }
        }
        detectAnnouncement(e.postingId(), e.stageId(), e.result());
    }

    @EventListener
    public void onChanged(RegistrationResultChangedEvent e) {
        // old는 항상 PENDING (PENDING→확정 한 방향)
        repository.movePendingToConfirmed(e.postingId(), e.stageId(), e.newResult().name());
        detectAnnouncement(e.postingId(), e.stageId(), e.newResult());
    }

    // PASS/FAIL 증가로 방금 임계값을 넘겼으면 발표일 기록
    private void detectAnnouncement(Long postingId, Long stageId, RegistrationResult result) {
        // PENDING은 PASS+FAIL을 안 늘리니 감지 불필요
        if (result == RegistrationResult.PENDING) return;

        Long confirmed = repository.findConfirmedCount(postingId, stageId);
        if (confirmed != null && confirmed >= THRESHOLD) {
            // 정확히 THRESHOLD가 된 순간 = 방금 돌파 → 오늘로 기록
            postingStageRepository.findById(stageId)
                    .ifPresent(stage -> stage.markAnnouncedIfAbsent(LocalDate.now()));
        }
    }
}