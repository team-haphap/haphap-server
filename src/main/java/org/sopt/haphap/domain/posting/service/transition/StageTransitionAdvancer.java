package org.sopt.haphap.domain.posting.service.transition;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.domain.StageType;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.registration.event.RegistrationApprovedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 전형 이동 정책: 다음 전형의 합격 인증이 1건 이상 승인되면 즉시 그 전형으로 전진한다.
 * 최소 시차·건수/시간창 조건은 폐지됨. 건너뛰기·역행은 orderIndex 인접성 체크로 막는다
 * (후속 전형 결과는 운영진이 직접 수동 이동 — 자동 이동 대상이 아님).
 *
 * currentStage가 아직 없는 공고(초기화 전)는 건드리지 않는다.
 * 동일 공고에 동시 요청이 들어와도, 이미 전진한 뒤에는 candidate가 더 이상 "바로 다음 전형"이 아니게 되어
 * 재이동이 자연스럽게 막힌다(최초 1회만 반영).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StageTransitionAdvancer {

    private final PostingRepository postingRepository;
    private final PostingStageRepository postingStageRepository;

    @EventListener
    public void onApproved(RegistrationApprovedEvent e) {
        Posting posting = postingRepository.findById(e.postingId())
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 공고입니다: " + e.postingId()));

        PostingStage current = posting.getCurrentStage();
        if (current == null || current.getStageType() == StageType.FINAL_PASS) {
            return;   // 미초기화 상태거나 이미 마지막 전형 → 더 이동할 곳 없음
        }

        PostingStage candidate = postingStageRepository.findById(e.stageId())
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 전형입니다: " + e.stageId()));

        if (!isImmediateNext(current, candidate)) {
            return;   // 이전/후속 전형이면 무시 — 후속은 운영진 수동 이동 대상
        }

        advance(posting, candidate);
    }

    private boolean isImmediateNext(PostingStage current, PostingStage candidate) {
        return candidate.getOrderIndex() == current.getOrderIndex() + 1;
    }

    private void advance(Posting posting, PostingStage next) {
        next.markMoved(LocalDateTime.now());
        posting.moveCurrentStageTo(next);
        log.info("전형 이동: postingId={}, nextStageId={}({})", posting.getId(), next.getId(), next.getStageType());
    }
}
