package org.sopt.haphap.domain.posting.service.calculator;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.code.PostingErrorCode;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Component;

// 현재 진행 전형 판정을 담당 (재사용). Posting.currentStage/isClosed() 기반 신정책.
@Component
@RequiredArgsConstructor
public class CurrentStageResolver {

    private final PostingRepository postingRepository;

    // 현재 진행 여부
    public String resolveCurrentState(Long postingId) {
        Posting posting = findPosting(postingId);
        if (posting.isClosed()) {
            return "마감";
        }

        PostingStage current = posting.getCurrentStage();
        if (current == null) {
            return "진행 예정";
        }
        return current.getName() + " 진행 중";
    }

    public String resolveCurrentStageName(Long postingId) {
        Posting posting = findPosting(postingId);
        if (posting.isClosed()) {
            return null;   // 마감 → 알람 없음
        }

        PostingStage current = posting.getCurrentStage();
        return current == null ? null : current.getName();
    }

    private Posting findPosting(Long postingId) {
        return postingRepository.findById(postingId)
                .orElseThrow(() -> new CustomException(PostingErrorCode.POSTING_NOT_FOUND));
    }
}
