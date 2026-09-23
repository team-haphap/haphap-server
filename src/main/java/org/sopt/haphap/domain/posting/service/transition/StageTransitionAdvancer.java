package org.sopt.haphap.domain.posting.service.transition;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.domain.StageType;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.posting.service.calculator.StageTransitionPolicy;
import org.sopt.haphap.domain.registration.domain.RegistrationResult;
import org.sopt.haphap.domain.registration.event.RegistrationResultChangedEvent;
import org.sopt.haphap.domain.registration.event.StageResultCountedEvent;
import org.sopt.haphap.domain.registration.service.RegistrationQueryService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 전형 이동 신정책: 현재 전형의 "바로 다음 전형"이
 * (1) 최근 1시간 동안 확정(PASS/FAIL) 결과가 5건 이상이고
 * (2) 현재 전형이 된 시점으로부터 StageTransitionPolicy가 정한 최소 시차가 지났으면
 * Posting.currentStage를 그 다음 전형으로 전진시킨다.
 *
 * currentStage가 아직 없는 공고(초기화 전)는 건드리지 않는다 — 초기화는 별도 단계에서 처리한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StageTransitionAdvancer {

    private static final int WINDOW_THRESHOLD = 5;
    private static final Duration WINDOW = Duration.ofHours(1);

    private final PostingRepository postingRepository;
    private final PostingStageRepository postingStageRepository;
    private final RegistrationQueryService registrationQueryService;
    private final StageTransitionPolicy stageTransitionPolicy;

    @EventListener
    public void onCreated(StageResultCountedEvent e) {
        tryAdvance(e.postingId(), e.stageId(), e.result());
    }

    @EventListener
    public void onChanged(RegistrationResultChangedEvent e) {
        tryAdvance(e.postingId(), e.stageId(), e.newResult());
    }

    private void tryAdvance(Long postingId, Long stageId, RegistrationResult result) {
        if (result == RegistrationResult.PENDING) {
            return;
        }

        Posting posting = postingRepository.findById(postingId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 공고입니다: " + postingId));

        PostingStage current = posting.getCurrentStage();
        if (current == null || current.getStageType() == StageType.FINAL_PASS) {
            return;   // 미초기화 상태거나 이미 마지막 전형 → 더 이동할 곳 없음
        }

        PostingStage candidate = postingStageRepository.findById(stageId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 전형입니다: " + stageId));

        if (!isImmediateNext(current, candidate)) {
            return;   // 현재 전형의 바로 다음 전형이 아니면 무시 (건너뛰기/역행 방지)
        }
        if (!minGapElapsed(current)) {
            return;
        }
        if (!enoughConfirmedInWindow(postingId, stageId)) {
            return;
        }

        advance(posting, candidate);
    }

    private boolean isImmediateNext(PostingStage current, PostingStage candidate) {
        return candidate.getOrderIndex() == current.getOrderIndex() + 1;
    }

    private boolean minGapElapsed(PostingStage current) {
        if (current.getMovedAt() == null) {
            return false;   // 현재 전형이 된 시점을 모르면 안전하게 보류
        }
        Duration minGap = stageTransitionPolicy.minGapFrom(current.getStageType());
        return !LocalDateTime.now().isBefore(current.getMovedAt().plus(minGap));
    }

    private boolean enoughConfirmedInWindow(Long postingId, Long stageId) {
        LocalDateTime since = LocalDateTime.now().minus(WINDOW);
        return registrationQueryService.countConfirmedSince(postingId, stageId, since) >= WINDOW_THRESHOLD;
    }

    private void advance(Posting posting, PostingStage next) {
        next.markMoved(LocalDateTime.now());
        posting.moveCurrentStageTo(next);
        log.info("전형 이동: postingId={}, nextStageId={}({})", posting.getId(), next.getId(), next.getStageType());
    }
}
