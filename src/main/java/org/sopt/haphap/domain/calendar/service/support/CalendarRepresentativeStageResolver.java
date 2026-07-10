package org.sopt.haphap.domain.calendar.service.support;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.calendar.service.CalendarStageMerger;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageCalendarProjection;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageFlatProjection;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.posting.repository.StageResultCountRepository;
import org.sopt.haphap.domain.posting.service.calculator.NextStageCalculator;
import org.sopt.haphap.domain.registration.projection.StageRegistrationCountProjection;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CalendarRepresentativeStageResolver {

    private final PostingStageRepository postingStageRepository;
    private final StageResultCountRepository stageResultCountRepository;
    private final NextStageCalculator nextStageCalculator;

    /*공고별로 그 날짜에 겹치는 전형 후보 중 대표 전형 하나를 고른다 - 최신걸로 선택 */
    public Map<Long, PostingStageCalendarProjection> resolve(List<PostingStageCalendarProjection> stages) {
        Map<Long, List<PostingStageCalendarProjection>> byPosting = stages.stream()
                .collect(Collectors.groupingBy(PostingStageCalendarProjection::getPostingId));

        List<Long> conflictPostingIds = byPosting.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .toList();

        // 겹치는 공고가 없으면 등록 카운트 쿼리 자체를 안 날림
        Map<Long, PostingStageFlatProjection> currentStageByPosting =
                conflictPostingIds.isEmpty() ? Map.of() : loadCurrentStages(conflictPostingIds);

        Map<Long, PostingStageCalendarProjection> result = new HashMap<>();
        byPosting.forEach((postingId, candidates) -> result.put(postingId,
                candidates.size() == 1
                        ? candidates.get(0)
                        : pickRepresentative(candidates, currentStageByPosting.get(postingId))));
        return result;
    }

    // 충돌난 공고만 배치로 전체 전형 + 확정등록수 로딩 → 실제 진행 전형 계산
    private Map<Long, PostingStageFlatProjection> loadCurrentStages(List<Long> postingIds) {
        Map<Long, List<PostingStageFlatProjection>> stagesByPosting = postingStageRepository
                .findFlatByPostingIds(postingIds).stream()
                .collect(Collectors.groupingBy(PostingStageFlatProjection::getPostingId));
        stagesByPosting.values().forEach(l ->
                l.sort(Comparator.comparingInt(PostingStageFlatProjection::getOrderIndex)));

        Map<Long, Map<Long, Long>> countsByPosting = stageResultCountRepository
                .findTotalsByPostingIds(postingIds).stream()
                .collect(Collectors.groupingBy(
                        StageRegistrationCountProjection::getPostingId,
                        Collectors.toMap(
                                StageRegistrationCountProjection::getStageId,
                                StageRegistrationCountProjection::getCnt)));

        Map<Long, PostingStageFlatProjection> result = new HashMap<>();
        for (Long postingId : postingIds) {
            List<PostingStageFlatProjection> stageList = stagesByPosting.getOrDefault(postingId, List.of());
            PostingStageFlatProjection current =
                    nextStageCalculator.currentStage(stageList, countsByPosting.getOrDefault(postingId, Map.of()));
            if (current != null) result.put(postingId, current);
        }
        return result;
    }

    private PostingStageCalendarProjection pickRepresentative(
            List<PostingStageCalendarProjection> candidates,
            PostingStageFlatProjection currentStage) {
        if (currentStage != null) {
            Optional<PostingStageCalendarProjection> matched = candidates.stream()
                    .filter(c -> c.getStageId().equals(currentStage.getStageId()))
                    .findFirst();
            if (matched.isPresent()) return matched.get();
        }
        return candidates.stream()
                .reduce(CalendarStageMerger::pickHigherScore)
                .orElseThrow();
    }
}