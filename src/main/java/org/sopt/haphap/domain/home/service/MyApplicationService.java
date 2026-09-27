package org.sopt.haphap.domain.home.service;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.home.dto.response.MyApplicationListResponse;
import org.sopt.haphap.domain.home.dto.response.MyApplicationResponse;
import org.sopt.haphap.domain.posting.domain.CompanyImageType;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.service.support.PostingAggregate;
import org.sopt.haphap.domain.posting.service.support.PostingAggregateLoader;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 홈 [내 지원]: 유저가 결과를 등록한 공고를, 그 공고에 마지막으로 활동한 시각(최근 상태 등록순) 기준
 * 최신순으로 최대 3개까지. 마감된 공고는 제외.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyApplicationService {

    private static final int MAX_APPLICATIONS = 3;
    private static final String STATUS_SUFFIX = " 발표 중";

    private final RegistrationRepository registrationRepository;
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
        var display = posting.resolveDisplay();
        PostingStage stage = display.stage();

        return new MyApplicationResponse(
                posting.getId(),
                posting.getCompany().getName(),
                posting.getTitle(),
                posting.getPosition(),
                stage == null ? null : stage.getName() + STATUS_SUFFIX,
                display.label(),
                agg.companyImageUrl(posting.getId()));
    }
}
