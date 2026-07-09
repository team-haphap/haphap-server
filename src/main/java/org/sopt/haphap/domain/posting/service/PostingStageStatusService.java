package org.sopt.haphap.domain.posting.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.code.PostingErrorCode;
import org.sopt.haphap.domain.posting.domain.StageStatus;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageFlatProjection;
import org.sopt.haphap.domain.posting.dto.response.PostingStageStatusListResponse;
import org.sopt.haphap.domain.posting.dto.response.PostingStageStatusResponse;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.posting.repository.StageResultCountRepository;
import org.sopt.haphap.domain.posting.service.calculator.NextStageCalculator;
import org.sopt.haphap.domain.registration.dto.StageRegistrationCountProjection;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostingStageStatusService {

    private final PostingRepository postingRepository;
    private final PostingStageRepository postingStageRepository;
    private final StageResultCountRepository stageResultCountRepository;
    private final NextStageCalculator nextStageCalculator;

    public PostingStageStatusListResponse getStagesStatus(Long postingId) {
        // 공고 존재 검증
        if (!postingRepository.existsById(postingId)) {
            throw new CustomException(PostingErrorCode.POSTING_NOT_FOUND);
        }

        // 전형 목록 (orderIndex 순)
        List<PostingStageFlatProjection> stages =
                postingStageRepository.findFlatByPostingIds(List.of(postingId));

        if (stages.isEmpty()) {
            return PostingStageStatusListResponse.of(List.of(), null);
        }

        // 전형별 누적 카운트 (집계 테이블, PASS+FAIL)
        Map<Long, Long> counts = stageResultCountRepository
                .findTotalsByPostingIds(List.of(postingId)).stream()
                .collect(Collectors.toMap(
                        StageRegistrationCountProjection::getStageId,
                        StageRegistrationCountProjection::getCnt));

        // 현재 진행 전형 (재사용)
        PostingStageFlatProjection current = nextStageCalculator.currentStage(stages, counts);
        Long currentStageId = (current == null) ? null : current.getStageId();
        int currentOrder = (current == null) ? Integer.MAX_VALUE : current.getOrderIndex();

        // 각 전형에 상태 매핑
        List<PostingStageStatusResponse> result = stages.stream()
                .map(s -> new PostingStageStatusResponse(
                        s.getStageId(), s.getName(), s.getOrderIndex(),
                        resolveStatus(s.getOrderIndex(), currentOrder)))
                .toList();

        return PostingStageStatusListResponse.of(result, currentStageId);
    }

    private StageStatus resolveStatus(int stageOrder, int currentOrder) {
        if (stageOrder < currentOrder) return StageStatus.COMPLETED;
        if (stageOrder == currentOrder) return StageStatus.IN_PROGRESS;
        return StageStatus.UPCOMING;
    }
}
