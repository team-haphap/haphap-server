package org.sopt.haphap.domain.home.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.home.dto.response.RecentViewResponse;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.domain.StageType;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.springframework.stereotype.Component;

/**
 * 홈 화면 공고 카드 공통 조립 로직: D-day는 currentStage 자신이 아니라 다음 전형(next) 기준.
 * [최근 조회한 공고]/[지금 많이 보는 공고]에서 공유.
 */
@Component
@RequiredArgsConstructor
public class HomeCardAssembler {

    private static final String CLOSED_LABEL = "마감";

    private final PostingStageRepository postingStageRepository;

    public RecentViewResponse assemble(Posting posting, String logoImageUrl) {
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
                nextStageName, dDayLabel, logoImageUrl);
    }
}
