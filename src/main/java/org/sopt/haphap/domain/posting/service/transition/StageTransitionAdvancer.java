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
 * 전형 이동 정책: currentStage는 "발표를 기다리는 중인 전형"을 뜻한다.
 * 현재 전형 자신의 합격 인증이 승인되면(=그 전형 결과가 실제로 나왔다는 뜻) 바로 다음 전형으로 전진한다
 * (건너뛰기·역행은 orderIndex 인접성으로 막음 — 이전/후속 전형에 대한 승인은 무시하고 운영진 수동 이동 대상으로 남긴다).
 *
 * 최종합격(FINAL_PASS)은 그 다음이 없어서 "전진"할 곳이 없다. 대신 FINAL_PASS 자신의 합격이 승인되는
 * 순간을 "진짜 최종합격 확정 시각"으로 movedAt에 다시 찍어둔다 — Posting.isClosed()의 +4일 마감 카운트다운이
 * 이 시각을 기준으로 도니까, 여기서 놓치면 아무도 최종합격 안 났는데 마감돼버리는 문제가 생긴다.
 *
 * currentStage가 아직 없는 공고(초기화 전)는 건드리지 않는다.
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
        if (current == null) {
            return;   // 미초기화 상태 → 더 판단할 기준이 없음
        }

        if (!e.stageId().equals(current.getId())) {
            return;   // 현재(발표 대기 중인) 전형 자신의 승인이 아니면 무시 — 이전/후속 전형은 운영진 수동 이동 대상
        }

        if (current.getStageType() == StageType.FINAL_PASS) {
            confirmFinalPass(posting, current);
            return;
        }

        PostingStage next = postingStageRepository
                .findByPostingIdAndOrderIndex(posting.getId(), current.getOrderIndex() + 1)
                .orElse(null);
        if (next == null) {
            return;   // 다음 전형이 아직 등록 안 됨 → 이동 보류
        }

        advance(posting, next);
    }

    private void advance(Posting posting, PostingStage next) {
        next.markMoved(LocalDateTime.now());
        posting.moveCurrentStageTo(next);
        log.info("전형 이동: postingId={}, nextStageId={}({})", posting.getId(), next.getId(), next.getStageType());
    }

    private void confirmFinalPass(Posting posting, PostingStage finalStage) {
        finalStage.markMoved(LocalDateTime.now());
        log.info("최종합격 확정: postingId={}, stageId={}", posting.getId(), finalStage.getId());
    }
}
