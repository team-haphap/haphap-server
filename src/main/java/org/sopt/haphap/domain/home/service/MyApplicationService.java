package org.sopt.haphap.domain.home.service;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.home.dto.response.MyApplicationListResponse;
import org.sopt.haphap.domain.home.dto.response.MyApplicationResponse;
import org.sopt.haphap.domain.posting.domain.CompanyImageType;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.domain.StageType;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.posting.service.support.PostingAggregate;
import org.sopt.haphap.domain.posting.service.support.PostingAggregateLoader;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 홈 [내 지원]: 유저가 결과를 등록한 공고를, 그 공고에 마지막으로 활동한 시각(최근 상태 등록순) 기준
 * 최신순으로 최대 3개까지. 마감된 공고는 제외.
 *
 * "D-day는 다음 전형 발표 예상일" — 현재 전형(currentStageStatus)과 D-day 기준 전형이 서로 다르다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyApplicationService {

    private static final int MAX_APPLICATIONS = 3;
    private static final String STATUS_SUFFIX = " 발표 중";

    private final RegistrationRepository registrationRepository;
    private final PostingStageRepository postingStageRepository;
    private final PostingAggregateLoader aggregateLoader;

    public MyApplicationListResponse getMyApplications(Long userId) {
        List<Long> postingIds = registrationRepository.findAppliedPostingIdsOrderByLastActivity(userId);
        if (postingIds.isEmpty()) {
            return MyApplicationListResponse.from(List.of());
        }

        PostingAggregate agg = aggregateLoader.load(postingIds, CompanyImageType.POPULAR);

        List<MyApplicationResponse> result = postingIds.stream()
                .map(agg::posting)
                .filter(Objects::nonNull)
                .filter(posting -> !posting.isClosed())
                .limit(MAX_APPLICATIONS)
                .map(posting -> toResponse(posting, agg))
                .toList();

        return MyApplicationListResponse.from(result);
    }

    private MyApplicationResponse toResponse(Posting posting, PostingAggregate agg) {
        PostingStage current = posting.getCurrentStage();

        return new MyApplicationResponse(
                posting.getId(),
                posting.getCompany().getName(),
                posting.getTitle(),
                posting.getPosition(),
                current == null ? null : current.getName() + STATUS_SUFFIX,
                resolveNextStageDDay(posting, current),
                agg.companyImageUrl(posting.getId()));
    }

    // 다음 전형(currentStage 바로 다음)의 예상 발표일 기준 D-day. 최종합격이거나 다음 전형이 아직
    // 없으면 표시할 게 없다.
    private String resolveNextStageDDay(Posting posting, PostingStage current) {
        if (current == null || current.getStageType() == StageType.FINAL_PASS) {
            return null;
        }
        return postingStageRepository
                .findByPostingIdAndOrderIndex(posting.getId(), current.getOrderIndex() + 1)
                .map(next -> Posting.dDayLabelFor(next.getExpectedAnnouncementDate()))
                .orElse(null);
    }
}
