package org.sopt.haphap.domain.posting.service;

import jakarta.transaction.Transactional;
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
        PostingStage stage = postingStageRepository.save(PostingStage.create(
                request.name(), request.orderIndex(), request.expectedAnnouncementDate(),
                request.expectedScore(), posting));
        return PostingStageAdminResponse.from(stage);
    }
}