package org.sopt.haphap.domain.posting.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.code.PostingErrorCode;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.domain.StageStatus;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageFlatProjection;
import org.sopt.haphap.domain.posting.dto.response.PostingStageStatusListResponse;
import org.sopt.haphap.domain.posting.dto.response.PostingStageStatusResponse;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostingStageStatusService {

    private final PostingRepository postingRepository;
    private final PostingStageRepository postingStageRepository;

    public PostingStageStatusListResponse getStagesStatus(Long postingId) {
        Posting posting = postingRepository.findById(postingId)
                .orElseThrow(() -> new CustomException(PostingErrorCode.POSTING_NOT_FOUND));

        // 전형 목록 (orderIndex 순)
        List<PostingStageFlatProjection> stages =
                postingStageRepository.findFlatByPostingIds(List.of(postingId));

        if (stages.isEmpty()) {
            return PostingStageStatusListResponse.of(List.of(), null);
        }

        // 현재 진행 전형 (신정책: Posting.currentStage)
        PostingStage current = posting.getCurrentStage();
        boolean closed = posting.isClosed();

        // 기본 선택: 진행 중 전형, 없으면 첫 전형 (stages.isEmpty()는 위에서 이미 반환됨)
        Long defaultSelectedStageId = (current != null)
                ? current.getId()
                : stages.get(0).getStageId();
        // 각 전형에 상태 매핑
        List<PostingStageStatusResponse> result = stages.stream()
                .map(s -> new PostingStageStatusResponse(
                        s.getStageId(), s.getName(), s.getOrderIndex(),
                        resolveStatus(s.getOrderIndex(), current, closed)))
                .toList();

        return PostingStageStatusListResponse.of(result, defaultSelectedStageId);
    }

    private StageStatus resolveStatus(int stageOrder, PostingStage current, boolean closed) {
        if (closed) return StageStatus.COMPLETED;
        if (current == null) return StageStatus.UPCOMING;
        int currentOrder = current.getOrderIndex();
        if (stageOrder < currentOrder) return StageStatus.COMPLETED;
        if (stageOrder == currentOrder) return StageStatus.IN_PROGRESS;
        return StageStatus.UPCOMING;
    }
}
