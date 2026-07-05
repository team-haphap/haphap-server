package org.sopt.haphap.domain.posting.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.domain.StageResultCount;
import org.sopt.haphap.domain.posting.repository.StageResultCountRepository;
import org.sopt.haphap.domain.registration.event.RegistrationResultChangedEvent;
import org.sopt.haphap.domain.registration.event.StageResultCountedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StageResultCountUpdater {

    private final StageResultCountRepository repository;

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
    }

    @EventListener
    public void onChanged(RegistrationResultChangedEvent e) {
        // old는 항상 PENDING (PENDING→확정 한 방향)
        repository.movePendingToConfirmed(e.postingId(), e.stageId(), e.newResult().name());
    }
}