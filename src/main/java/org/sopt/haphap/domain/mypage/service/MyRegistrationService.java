package org.sopt.haphap.domain.mypage.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.mypage.dto.response.MyRegistrationPreviewListResponse;
import org.sopt.haphap.domain.mypage.dto.response.MyRegistrationPreviewResponse;
import org.sopt.haphap.domain.posting.domain.CompanyImageType;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageFlatProjection;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.posting.service.support.PostingAggregate;
import org.sopt.haphap.domain.posting.service.support.PostingAggregateLoader;
import org.sopt.haphap.domain.registration.projection.AppliedPostingProjection;
import org.sopt.haphap.domain.registration.projection.PostingParticipantCountProjection;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 마이페이지 [내 지원 내역] 미리보기: 유저가 결과를 등록한 공고를 최근 등록/수정 순으로 최대 10개.
 * 홈 [내 지원]과 같은 "유효한 결과" 기준(승인 전 합격은 제외)을 쓰지만, 마감된 공고도 포함한다.
 *
 * 상태 문구는 "다음 발표 예상 전형" 기준 — 현재 전형 바로 다음 전형의 "{전형명} 발표 예상".
 * 공고 수와 무관하게 쿼리 5번(대상 공고 → 공고·회사·카테고리·현재 전형 → 로고 → 참여 인원 → 전형 목록)으로 끝난다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyRegistrationService {

    private static final int PREVIEW_LIMIT = 10;
    private static final String EXPECTED_SUFFIX = " 발표 예상";
    private static final String CLOSED_STATUS = "마감";

    private final RegistrationRepository registrationRepository;
    private final PostingStageRepository postingStageRepository;
    private final PostingAggregateLoader aggregateLoader;

    public MyRegistrationPreviewListResponse getPreview(Long userId) {
        List<AppliedPostingProjection> applied = registrationRepository
                .findAppliedPostingsOrderByLastActivity(userId, PageRequest.of(0, PREVIEW_LIMIT));
        if (applied.isEmpty()) {
            return MyRegistrationPreviewListResponse.from(List.of());
        }

        List<Long> postingIds = applied.stream().map(AppliedPostingProjection::getPostingId).toList();
        PostingAggregate agg = aggregateLoader.load(postingIds, CompanyImageType.CALENDAR_LOGO);
        Map<Long, Long> applicantCounts = registrationRepository.countDistinctUsersByPostingIds(postingIds).stream()
                .collect(Collectors.toMap(
                        PostingParticipantCountProjection::getPostingId,
                        PostingParticipantCountProjection::getCnt));
        Map<Long, List<PostingStageFlatProjection>> stagesByPosting = postingStageRepository
                .findFlatByPostingIds(postingIds).stream()
                .collect(Collectors.groupingBy(PostingStageFlatProjection::getPostingId));

        List<MyRegistrationPreviewResponse> result = applied.stream()
                .filter(a -> agg.posting(a.getPostingId()) != null)
                .map(a -> toResponse(a, agg, applicantCounts, stagesByPosting))
                .toList();

        return MyRegistrationPreviewListResponse.from(result);
    }

    private MyRegistrationPreviewResponse toResponse(AppliedPostingProjection applied, PostingAggregate agg,
                                                     Map<Long, Long> applicantCounts,
                                                     Map<Long, List<PostingStageFlatProjection>> stagesByPosting) {
        Posting posting = agg.posting(applied.getPostingId());
        return new MyRegistrationPreviewResponse(
                posting.getId(),
                posting.getTitle(),
                resolveStageStatus(posting, stagesByPosting.getOrDefault(posting.getId(), List.of())),
                applicantCounts.getOrDefault(posting.getId(), 0L),
                agg.companyImageUrl(posting.getId()),
                applied.getLastActivityAt());
    }

    // 마감이면 "마감". 아니면 현재 전형보다 순서가 뒤인 첫 전형의 "{전형명} 발표 예상"
    // (현재 전형이 아직 없으면 첫 전형). 최종 전형이라 다음 전형이 없으면 null.
    private String resolveStageStatus(Posting posting, List<PostingStageFlatProjection> stages) {
        if (posting.isClosed()) {
            return CLOSED_STATUS;
        }
        PostingStage current = posting.getCurrentStage();
        int afterOrder = current == null ? Integer.MIN_VALUE : current.getOrderIndex();
        return stages.stream()
                .filter(s -> s.getOrderIndex() > afterOrder)
                .min(Comparator.comparingInt(PostingStageFlatProjection::getOrderIndex))
                .map(next -> next.getName() + EXPECTED_SUFFIX)
                .orElse(null);
    }
}
