package org.sopt.haphap.domain.posting.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.dto.response.PopularPostingListResponse;
import org.sopt.haphap.domain.posting.dto.response.PopularPostingResponse;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageFlatProjection;
import org.sopt.haphap.domain.posting.service.calculator.NextStageCalculator;
import org.sopt.haphap.domain.posting.service.support.PostingAggregate;
import org.sopt.haphap.domain.posting.service.support.PostingAggregateLoader;
import org.sopt.haphap.domain.posting.service.support.PostingResponseAssembler;
import org.sopt.haphap.domain.registration.domain.RegistrationResult;
import org.sopt.haphap.domain.registration.projection.StageRegistrationCountProjection;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopularPostingService {

    private static final int RECENT_HOURS = 48;
    private static final List<RegistrationResult> COUNTED_RESULTS =
            List.of(RegistrationResult.PASS, RegistrationResult.FAIL);
    private static final int MAX_POPULAR = 4;

    private final RegistrationRepository registrationRepository;
    private final PostingAggregateLoader aggregateLoader;
    private final PostingResponseAssembler assembler;
    private final NextStageCalculator nextStageCalculator;

    public PopularPostingListResponse getPopularPostings(List<String> categoryNames) {
        // "전체"이거나 비어있으면 null로 정규화 → 필터 미적용
        List<String> filter = (categoryNames == null || categoryNames.isEmpty())
                ? null : categoryNames;
        LocalDateTime since = LocalDateTime.now().minusHours(RECENT_HOURS);

        // 1) 48h 내 PASS/FAIL 결과 있는 공고 id목록
        List<Long> candidateIds = registrationRepository
                .findRecentlyActivePostingIds(COUNTED_RESULTS, since, filter);
        if (candidateIds.isEmpty()) {
            return PopularPostingListResponse.from(List.of());
        }
        // 2) 공통 배치 로딩 (공고·전형·누적등록수)
        PostingAggregate agg = aggregateLoader.load(candidateIds);

        // 3) 48h (공고,전형)별 등록수 — 필터 겸 정렬 기준
        Map<Long, Map<Long, Long>> recentCounts = registrationRepository
                .countRecentActiveByPostingAndStage(COUNTED_RESULTS, since, candidateIds).stream()
                .collect(Collectors.groupingBy(
                        StageRegistrationCountProjection::getPostingId,
                        Collectors.toMap(
                                StageRegistrationCountProjection::getStageId,
                                StageRegistrationCountProjection::getCnt)));

        // 4) 현재 진행 전형에 48h 활동 있는 것만 → 그 등록수로 내림차순 → 8개
        List<PopularPostingResponse> result = candidateIds.stream()
                .map(id -> toPopularScored(id, agg, recentCounts))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingLong(PopularScored::recentCount).reversed())
                .limit(MAX_POPULAR)
                .map(PopularScored::response)
                .toList();

        return PopularPostingListResponse.from(result);
    }

    private PopularScored toPopularScored(Long id, PostingAggregate agg,
                                          Map<Long, Map<Long, Long>> recentCounts) {
        List<PostingStageFlatProjection> stages = agg.stages(id);
        Map<Long, Long> counts = agg.counts(id);

        PostingStageFlatProjection current = nextStageCalculator.currentStage(stages, counts);
        if (current == null) return null;

        long recentCount = recentCounts.getOrDefault(id, Map.of())
                .getOrDefault(current.getStageId(), 0L);
        if (recentCount <= 0) return null;   // 현재 진행 전형에 48h 활동 없음 → 제외

        PopularPostingResponse response = assembler.assemble(agg.posting(id), stages, counts).response();
        return new PopularScored(response, recentCount);
    }

    private record PopularScored(PopularPostingResponse response, long recentCount) {}
}

