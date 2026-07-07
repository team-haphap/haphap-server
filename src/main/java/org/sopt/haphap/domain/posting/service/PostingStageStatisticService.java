package org.sopt.haphap.domain.posting.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.code.PostingErrorCode;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.dto.response.PostingStageStatisticResponse;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.posting.repository.StageResultCountRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostingStageStatisticService {

    private final PostingRepository postingRepository;
    private final PostingStageRepository postingStageRepository;
    private final StageResultCountRepository stageResultCountRepository;

    public PostingStageStatisticResponse getPostingStageStatistic(Long postingId, Long stageId) {
        if (!postingRepository.existsById(postingId)) {
            throw new CustomException(PostingErrorCode.POSTING_NOT_FOUND);
        }
        PostingStage stage = postingStageRepository.findById(stageId)
                .orElseThrow(() -> new CustomException(PostingErrorCode.STAGE_NOT_FOUND));
        if (!stage.getPosting().getId().equals(postingId)) {
            throw new CustomException(PostingErrorCode.STAGE_NOT_IN_POSTING);
        }

        // 집계 테이블에서 카운트 조회 (없으면 0)
        return stageResultCountRepository.findByPostingIdAndStageId(postingId, stageId)
                .map(count -> PostingStageStatisticResponse.of(stageId, count))
                .orElseGet(() -> PostingStageStatisticResponse.empty(stageId));

    }
}
