package org.sopt.haphap.domain.posting.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.code.PostingErrorCode;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.dto.response.PostingDetailResponse;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.registration.service.RegistrationQueryService;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostingDetailService {

    private static final int PROFILE_LIMIT = 4;
    private static final int FEED_LIMIT = 30;

    private final PostingRepository postingRepository;
    private final RegistrationQueryService registrationQueryService;
    private final CurrentStageResolver currentStageResolver;

    public PostingDetailResponse getDetail(Long postingId) {
        // 공고 + 회사 + 카테고리
        Posting posting = postingRepository.findWithCompanyAndCategory(postingId)
                .orElseThrow(() -> new CustomException(PostingErrorCode.POSTING_NOT_FOUND));

        // currentState 계산 (집계 테이블 재사용)
        String currentState = currentStageResolver.resolveCurrentState(postingId);
        var summary = registrationQueryService.getParticipantSummary(postingId, PROFILE_LIMIT);
        var feeds = registrationQueryService.getRecentFeeds(postingId, FEED_LIMIT);

        long additional = Math.max(0, summary.registeredCount() - PROFILE_LIMIT);

        return new PostingDetailResponse(
                posting.getCompany().getName(), posting.getTitle(), posting.getCategory().getName(),
                posting.getLocation(), posting.getPosition(), currentState,
                new PostingDetailResponse.SummaryResponse(
                        summary.registeredCount(), summary.profileImages(), additional),
                feeds.stream().map(f -> new PostingDetailResponse.RegistrationFeedResponse(
                        f.stage(), f.nickName(),f.status(), f.feedCreatedAt())).toList());

    }
}