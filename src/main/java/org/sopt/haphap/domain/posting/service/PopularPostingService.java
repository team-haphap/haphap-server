package org.sopt.haphap.domain.posting.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.dto.PopularPostingListResponse;
import org.sopt.haphap.domain.posting.dto.PopularPostingResponse;
import org.sopt.haphap.domain.posting.dto.PostingStageFlatProjection;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.registration.domain.RegistrationResult;
import org.sopt.haphap.domain.registration.dto.StageRegistrationCountProjection;
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
    private static final int MAX_POPULAR = 8;

    private final PostingRepository postingRepository;
    private final PostingStageRepository postingStageRepository;
    private final RegistrationRepository registrationRepository;
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

        // 2) 배치 조회
        // 공고+회사+카테고리
        Map<Long, Posting> postingMap = postingRepository
                .findAllWithCompanyAndCategoryByIds(candidateIds).stream()
                .collect(Collectors.toMap(Posting::getId, Function.identity()));

        // 공고별 전형 목록 (orderIndex 순 — 쿼리에서 정렬해둠)
        Map<Long, List<PostingStageFlatProjection>> stagesByPosting = postingStageRepository
                .findFlatByPostingIds(candidateIds).stream()
                .collect(Collectors.groupingBy(PostingStageFlatProjection::getPostingId));
        stagesByPosting.values()
                .forEach(list -> list.sort(Comparator.comparingInt(PostingStageFlatProjection::getOrderIndex)));


        // (공고, 전형)별 등록수 → 공고별 { stageId -> count }
        Map<Long, Map<Long, Long>> countsByPosting = registrationRepository
                .countByPostingAndStage(candidateIds).stream()
                .collect(Collectors.groupingBy(
                        StageRegistrationCountProjection::getPostingId,
                        Collectors.toMap(
                                StageRegistrationCountProjection::getStageId,
                                StageRegistrationCountProjection::getCnt)));
        // 2-1) 48h 내 (공고,전형)별 등록수 → 정렬 기준 겸 필터 판정에 사용
        Map<Long, Map<Long, Long>> recentCountsByPosting = registrationRepository
                .countRecentActiveByPostingAndStage(COUNTED_RESULTS, since, candidateIds).stream()
                .collect(Collectors.groupingBy(
                        StageRegistrationCountProjection::getPostingId,
                        Collectors.toMap(
                                StageRegistrationCountProjection::getStageId,
                                StageRegistrationCountProjection::getCnt)));
        // 3) 현재 진행 전형에 48h 활동 있는 것만 남기고, 정렬키(현재진행전형의 48h 등록수) 계산
        List<PopularScored> scored = candidateIds.stream()
                .map(id -> {
                    List<PostingStageFlatProjection> stages = stagesByPosting.getOrDefault(id, List.of());
                    Map<Long, Long> counts = countsByPosting.getOrDefault(id, Map.of());

                    PostingStageFlatProjection current = nextStageCalculator.currentStage(stages, counts);
                    if (current == null) return null;

                    // 현재 진행 전형의 48h 등록수
                    Map<Long, Long> recent = recentCountsByPosting.getOrDefault(id, Map.of());
                    long recentCount = recent.getOrDefault(current.getStageId(), 0L);

                    // 현재 진행 전형에 48h 활동 없으면 제외
                    if (recentCount <= 0) return null;

                    ScoredPosting base = buildScored(postingMap.get(id), stages, counts);
                    return new PopularScored(base.response(), recentCount);
                })
                .filter(Objects::nonNull)
                .toList();
        // 4) 등록수 많은 순 정렬 + 상위 8개
        List<PopularPostingResponse> result = scored.stream()
                .sorted(Comparator.comparingLong(PopularScored::recentCount).reversed())
                .limit(MAX_POPULAR)   // = 8
                .map(PopularScored::response)
                .toList();

        return PopularPostingListResponse.from(result);
    }

    private ScoredPosting buildScored(Posting posting,
                                      List<PostingStageFlatProjection> stages,
                                      Map<Long, Long> countByStageId) {
        PostingStageFlatProjection nextStage = nextStageCalculator.calculate(stages, countByStageId);
        Integer days = nextStageCalculator.daysUntil(nextStage);
        LocalDate announceDate = (nextStage == null) ? null : nextStage.getExpectedAnnouncementDate();

        PopularPostingResponse response = new PopularPostingResponse(
                posting.getId(),
                posting.getTitle(),
                posting.getCompany().getName(),
                posting.getCategory().getName(),
                posting.getCompany().getDescription(),
                nextStage == null ? null : nextStage.getName(),
                days,
                posting.getCompany().getImageUrl());

        return new ScoredPosting(response, posting.getTitle(), announceDate);
    }

    private int sortRank(LocalDate date, LocalDate today) {
        if (date == null) return 2;
        return date.isBefore(today) ? 1 : 0;
    }

    private record ScoredPosting(PopularPostingResponse response, String title, LocalDate announceDate) {
    }
    private record PopularScored(PopularPostingResponse response, long recentCount) {}
}

