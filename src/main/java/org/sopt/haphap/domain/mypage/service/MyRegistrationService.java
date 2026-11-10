package org.sopt.haphap.domain.mypage.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.mypage.dto.request.MyRegistrationFilter;
import org.sopt.haphap.domain.mypage.dto.response.MyRegistrationListResponse;
import org.sopt.haphap.domain.mypage.dto.response.MyRegistrationPreviewListResponse;
import org.sopt.haphap.domain.mypage.dto.response.MyRegistrationPreviewResponse;
import org.sopt.haphap.domain.mypage.dto.response.MyRegistrationResponse;
import org.sopt.haphap.domain.posting.domain.CompanyImageType;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.service.support.PostingAggregate;
import org.sopt.haphap.domain.posting.service.support.PostingAggregateLoader;
import org.sopt.haphap.domain.registration.projection.AppliedPostingProjection;
import org.sopt.haphap.domain.registration.projection.MyRegistrationRowProjection;
import org.sopt.haphap.domain.registration.projection.PostingParticipantCountProjection;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 마이페이지 [내 지원 내역] (미리보기 / 전체보기).
 * 홈 [내 지원]과 같은 "유효한 결과" 기준을 쓴다 — 합격은 운영진 인증 승인이 나야 등록한 것으로 친다.
 * 홈과 달리 마감된 공고도 포함한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyRegistrationService {

    private static final int PREVIEW_LIMIT = 10;
    private static final String CLOSED_STATE = "마감";
    private static final String UPCOMING_STATE = "진행 예정";

    private final RegistrationRepository registrationRepository;
    private final PostingAggregateLoader aggregateLoader;

    /**
     * 미리보기: 최근 등록/수정 순으로 최대 10개. 현재 전형은 전체보기와 같은 기준.
     * 쿼리 4번(대상 공고 → 공고·회사·카테고리·현재 전형 → 로고 → 참여 인원).
     */
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

        List<MyRegistrationPreviewResponse> result = applied.stream()
                .filter(a -> agg.posting(a.getPostingId()) != null)
                .map(a -> toResponse(a, agg, applicantCounts))
                .toList();

        return MyRegistrationPreviewListResponse.from(result);
    }

    /**
     * [내 지원 내역] 전체보기: 등록한 공고 전부를 최근 등록/수정 순으로. 탭 숫자(counts)는 필터와 무관하게 전체 기준.
     * 쿼리 3번(유저의 유효한 등록 전부 → 공고·회사·카테고리·현재 전형 → 로고). 필터링은 마감 판정이 시각 기반이라 메모리에서 한다.
     */
    public MyRegistrationListResponse getAll(Long userId, MyRegistrationFilter filter) {
        List<AppliedPosting> applied = groupByPosting(registrationRepository.findValidRegistrationRowsByUserId(userId));
        if (applied.isEmpty()) {
            return new MyRegistrationListResponse(new MyRegistrationListResponse.Counts(0, 0, 0), List.of());
        }

        List<Long> postingIds = applied.stream().map(AppliedPosting::postingId).toList();
        PostingAggregate agg = aggregateLoader.load(postingIds, CompanyImageType.CALENDAR_LOGO);

        // isClosed()는 호출 시각 기준이라 공고당 한 번만 판정해 counts와 필터가 같은 결과를 보게 한다
        List<AppliedPosting> existing = applied.stream().filter(a -> agg.posting(a.postingId()) != null).toList();
        Map<Long, Boolean> closedByPostingId = existing.stream()
                .collect(Collectors.toMap(AppliedPosting::postingId, a -> agg.posting(a.postingId()).isClosed()));

        long closedCount = closedByPostingId.values().stream().filter(Boolean::booleanValue).count();
        MyRegistrationListResponse.Counts counts = new MyRegistrationListResponse.Counts(
                existing.size(), existing.size() - closedCount, closedCount);

        List<MyRegistrationResponse> registrations = existing.stream()
                .filter(a -> filter.matches(closedByPostingId.get(a.postingId())))
                .map(a -> toResponse(a, agg.posting(a.postingId()), closedByPostingId.get(a.postingId()), agg))
                .toList();

        return new MyRegistrationListResponse(counts, registrations);
    }

    // 등록 행들을 공고별로 묶어 마지막 활동 시각과 전형 순서상 가장 뒤 결과를 뽑고, 최근 활동순으로 정렬
    private List<AppliedPosting> groupByPosting(List<MyRegistrationRowProjection> rows) {
        return rows.stream()
                .collect(Collectors.groupingBy(MyRegistrationRowProjection::getPostingId))
                .entrySet().stream()
                .map(e -> new AppliedPosting(
                        e.getKey(),
                        e.getValue().stream()
                                .map(MyRegistrationRowProjection::getUpdatedAt)
                                .filter(Objects::nonNull)
                                .max(Comparator.naturalOrder())
                                .orElse(null),
                        e.getValue().stream()
                                .max(Comparator.comparingInt(MyRegistrationRowProjection::getStageOrder))
                                .orElseThrow()))
                .sorted(Comparator.comparing(AppliedPosting::lastActivityAt,
                                Comparator.nullsLast(Comparator.<LocalDateTime>reverseOrder()))
                        .thenComparing(AppliedPosting::postingId, Comparator.reverseOrder()))
                .toList();
    }

    private MyRegistrationResponse toResponse(AppliedPosting applied, Posting posting, boolean closed,
                                              PostingAggregate agg) {
        MyRegistrationRowProjection latest = applied.latestStageRow();
        return new MyRegistrationResponse(
                posting.getId(),
                posting.getCompany().getName(),
                posting.getTitle(),
                agg.companyImageUrl(posting.getId()),
                resolveCurrentState(posting, closed),
                latest.getStageName() + " " + latest.getResult().getDescription());
    }

    // 공고 상세의 [현재 전형]과 같은 기준: 마감 → "마감", 현재 전형 없음 → "진행 예정", 그 외 현재 전형명
    private String resolveCurrentState(Posting posting, boolean closed) {
        if (closed) {
            return CLOSED_STATE;
        }
        PostingStage current = posting.getCurrentStage();
        return current == null ? UPCOMING_STATE : current.getName();
    }

    private record AppliedPosting(Long postingId, LocalDateTime lastActivityAt,
                                  MyRegistrationRowProjection latestStageRow) {}

    private MyRegistrationPreviewResponse toResponse(AppliedPostingProjection applied, PostingAggregate agg,
                                                     Map<Long, Long> applicantCounts) {
        Posting posting = agg.posting(applied.getPostingId());
        return new MyRegistrationPreviewResponse(
                posting.getId(),
                posting.getTitle(),
                resolveCurrentState(posting, posting.isClosed()),
                applicantCounts.getOrDefault(posting.getId(), 0L),
                agg.companyImageUrl(posting.getId()),
                applied.getLastActivityAt());
    }
}
