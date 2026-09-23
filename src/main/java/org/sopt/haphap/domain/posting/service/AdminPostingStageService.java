package org.sopt.haphap.domain.posting.service;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.code.PostingErrorCode;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.dto.request.PostingStageCreateRequest;
import org.sopt.haphap.domain.posting.dto.response.PostingStageAdminResponse;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminPostingStageService {

    private final PostingRepository postingRepository;
    private final PostingStageRepository postingStageRepository;

    @Transactional
    public PostingStageAdminResponse createStage(Long postingId, PostingStageCreateRequest request) {
        Posting posting = postingRepository.findById(postingId)
                .orElseThrow(() -> new CustomException(PostingErrorCode.POSTING_NOT_FOUND));
        if (postingStageRepository.existsByPostingIdAndOrderIndex(postingId, request.orderIndex())) {
            throw new CustomException(PostingErrorCode.DUPLICATE_STAGE_ORDER);
        }
        if (postingStageRepository.existsByPostingIdAndStageType(postingId, request.stageType())) {
            throw new CustomException(PostingErrorCode.DUPLICATE_STAGE_TYPE);
        }
        PostingStage stage = postingStageRepository.save(PostingStage.create(
                request.name(), request.orderIndex(), request.expectedAnnouncementDate(),
                request.expectedScore(), request.stageType(), posting));
        initializeCurrentStageIfFirst(posting, stage);
        return PostingStageAdminResponse.from(stage);
    }

    // 전형 이동 새로운정책: 1번 전형이 등록되는 시점에 currentStage를 그 전형으로 초기화
    // orderIndex는 생성 순서와 무관하게(1번을 나중에 등록해도) 값 자체로만 판단한다.
    private void initializeCurrentStageIfFirst(Posting posting, PostingStage stage) {
        if (stage.getOrderIndex() == 1 && posting.getCurrentStage() == null) {
            stage.markMoved(LocalDateTime.now());
            posting.moveCurrentStageTo(stage);
        }
    }
}