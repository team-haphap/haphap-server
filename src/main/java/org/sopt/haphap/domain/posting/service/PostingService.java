package org.sopt.haphap.domain.posting.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.code.PostingErrorCode;
import org.sopt.haphap.domain.posting.dto.response.PostingListResponse;
import org.sopt.haphap.domain.posting.dto.response.PostingStageListResponse;
import org.sopt.haphap.domain.posting.dto.response.PostingStageResponse;
import org.sopt.haphap.domain.posting.dto.response.PostingSummaryResponse;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostingService {

    private final PostingRepository postingRepository;
    private final PostingStageRepository postingStageRepository;

    public PostingListResponse getPostings() {
        List<PostingSummaryResponse> postings = postingRepository.findAllOrderByTitleAsc();
        return PostingListResponse.from(postings);
    }

    public PostingStageListResponse getStages(Long postingId) {
        if (!postingRepository.existsById(postingId)) {
            throw new CustomException(PostingErrorCode.POSTING_NOT_FOUND);
        }
        List<PostingStageResponse> stages = postingStageRepository.findStagesByPostingId(postingId);
        return PostingStageListResponse.of(postingId, stages);
    }
}
