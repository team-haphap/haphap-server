package org.sopt.haphap.domain.home.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.home.dto.response.RecentViewListResponse;
import org.sopt.haphap.domain.home.dto.response.RecentViewResponse;
import org.sopt.haphap.domain.home.repository.PostingViewHistoryRepository;
import org.sopt.haphap.domain.posting.domain.CompanyImageType;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.domain.StageType;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.posting.service.support.PostingAggregate;
import org.sopt.haphap.domain.posting.service.support.PostingAggregateLoader;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 홈 [최근 조회한 공고]: 최대 10개, 30일 이내 조회분만, 마감된 공고도 노출.
 * D-day는 currentStage 자신이 아니라 다음 전형(next) 기준 — [내 지원]과 동일한 규칙.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecentViewService {

    private static final int MAX_RECENT_VIEWS = 10;
    private static final int RETENTION_DAYS = 30;
    private static final String CLOSED_LABEL = "마감";

    private final PostingViewHistoryRepository postingViewHistoryRepository;
    private final PostingStageRepository postingStageRepository;
    private final PostingAggregateLoader aggregateLoader;

    public RecentViewListResponse getRecentViews(Long userId) {
        LocalDateTime since = LocalDateTime.now().minusDays(RETENTION_DAYS);
        List<Long> postingIds = postingViewHistoryRepository
                .findRecentPostingIds(userId, since, PageRequest.of(0, MAX_RECENT_VIEWS));
        if (postingIds.isEmpty()) {
            return RecentViewListResponse.from(List.of());
        }

        PostingAggregate agg = aggregateLoader.load(postingIds, CompanyImageType.POPULAR);

        List<RecentViewResponse> result = postingIds.stream()
                .map(agg::posting)
                .filter(Objects::nonNull)
                .map(posting -> toResponse(posting, agg))
                .toList();

        return RecentViewListResponse.from(result);
    }

    private RecentViewResponse toResponse(Posting posting, PostingAggregate agg) {
        String nextStageName = null;
        String dDayLabel = null;

        if (posting.isClosed()) {
            dDayLabel = CLOSED_LABEL;
        } else {
            PostingStage current = posting.getCurrentStage();
            if (current != null && current.getStageType() != StageType.FINAL_PASS) {
                PostingStage next = postingStageRepository
                        .findByPostingIdAndOrderIndex(posting.getId(), current.getOrderIndex() + 1)
                        .orElse(null);
                if (next != null) {
                    nextStageName = next.getName();
                    dDayLabel = Posting.dDayLabelFor(next.getExpectedAnnouncementDate());
                }
            }
        }

        return new RecentViewResponse(
                posting.getId(), posting.getCompany().getName(), posting.getTitle(), posting.getPosition(),
                nextStageName, dDayLabel, agg.companyImageUrl(posting.getId()));
    }
}
