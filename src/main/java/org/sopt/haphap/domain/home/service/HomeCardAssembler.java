package org.sopt.haphap.domain.home.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.home.dto.response.RecentViewResponse;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.domain.StageType;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageFlatProjection;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.posting.service.support.PostingAggregate;
import org.springframework.stereotype.Component;

/**
 * 홈 화면 공고 카드 공통 조립 로직: D-day는 currentStage 자신이 아니라 다음 전형(next) 기준.
 * [최근 조회한 공고]/[지금 많이 보는 공고]에서 공유.
 * 다음 전형 조회는 postingId별 개별 쿼리(N+1) 대신 findFlatByPostingIds로 한 번에 배치 조회한다.
 */
@Component
@RequiredArgsConstructor
public class HomeCardAssembler {

    private static final String CLOSED_LABEL = "마감";

    private final PostingStageRepository postingStageRepository;

    public List<RecentViewResponse> assemble(List<Posting> postings, PostingAggregate agg) {
        Map<Long, List<PostingStageFlatProjection>> stagesByPostingId = loadStages(postings);
        return postings.stream()
                .map(posting -> assembleOne(posting, agg.companyImageUrl(posting.getId()), stagesByPostingId))
                .toList();
    }

    private Map<Long, List<PostingStageFlatProjection>> loadStages(List<Posting> postings) {
        List<Long> postingIds = postings.stream()
                .filter(posting -> !posting.isClosed())
                .map(Posting::getId)
                .toList();
        if (postingIds.isEmpty()) {
            return Map.of();
        }
        return postingStageRepository.findFlatByPostingIds(postingIds).stream()
                .collect(Collectors.groupingBy(PostingStageFlatProjection::getPostingId));
    }

    private RecentViewResponse assembleOne(
            Posting posting, String logoImageUrl, Map<Long, List<PostingStageFlatProjection>> stagesByPostingId) {
        String nextStageName = null;
        String dDayLabel = null;

        if (posting.isClosed()) {
            dDayLabel = CLOSED_LABEL;
        } else {
            PostingStage current = posting.getCurrentStage();
            if (current != null && current.getStageType() != StageType.FINAL_PASS) {
                int nextOrderIndex = current.getOrderIndex() + 1;
                PostingStageFlatProjection next = stagesByPostingId
                        .getOrDefault(posting.getId(), List.of()).stream()
                        .filter(stage -> stage.getOrderIndex() == nextOrderIndex)
                        .findFirst()
                        .orElse(null);
                if (next != null) {
                    nextStageName = next.getName();
                    dDayLabel = Posting.dDayLabelFor(next.getExpectedAnnouncementDate());
                }
            }
        }

        return new RecentViewResponse(
                posting.getId(), posting.getCompany().getName(), posting.getTitle(),
                posting.getCategory().getName(),
                //posting.getPosition(),
                nextStageName, dDayLabel, logoImageUrl);
    }
}
