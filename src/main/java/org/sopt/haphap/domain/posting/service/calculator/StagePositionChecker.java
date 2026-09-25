package org.sopt.haphap.domain.posting.service.calculator;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.springframework.stereotype.Component;

// 어떤 전형이 현재 전형 기준으로 "후속 전형"(다음 전형보다도 더 뒤)인지 판단.
@Component
@RequiredArgsConstructor
public class StagePositionChecker {

    private final PostingRepository postingRepository;
    private final PostingStageRepository postingStageRepository;

    public boolean isSubsequentStage(Long postingId, Long stageId) {
        Posting posting = postingRepository.findById(postingId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 공고입니다: " + postingId));
        PostingStage current = posting.getCurrentStage();
        if (current == null) {
            return false;   // currentStage 미초기화 상태면 후속 여부를 판단할 기준이 없음
        }
        PostingStage target = postingStageRepository.findById(stageId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 전형입니다: " + stageId));
        return target.getOrderIndex() > current.getOrderIndex() + 1;
    }
}
