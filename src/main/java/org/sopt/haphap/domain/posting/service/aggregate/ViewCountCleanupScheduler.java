package org.sopt.haphap.domain.posting.service.aggregate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Comparator;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageFlatProjection;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.posting.repository.StageResultCountRepository;
import org.sopt.haphap.domain.posting.service.calculator.NextStageCalculator;
import org.sopt.haphap.domain.registration.projection.StageRegistrationCountProjection;
import org.sopt.haphap.domain.posting.service.PostingViewTracker;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountCleanupScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final PostingStageRepository postingStageRepository;
    private final StageResultCountRepository stageResultCountRepository;
    private final NextStageCalculator nextStageCalculator;

    @Scheduled(cron = "0 0 0 * * *")
    public void removeClosedPostings() {
        Map<Long, List<PostingStageFlatProjection>> stagesByPosting = postingStageRepository
                .findAllStages().stream()
                .collect(Collectors.groupingBy(PostingStageFlatProjection::getPostingId));
        stagesByPosting.values()
                .forEach(list -> list.sort(Comparator.comparingInt(PostingStageFlatProjection::getOrderIndex)));

        Map<Long, Map<Long, Long>> countsByPosting = stageResultCountRepository
                .findAllTotals().stream()
                .collect(Collectors.groupingBy(
                        StageRegistrationCountProjection::getPostingId,
                        Collectors.toMap(
                                StageRegistrationCountProjection::getStageId,
                                StageRegistrationCountProjection::getCnt)));

        List<String> closedIds = stagesByPosting.entrySet().stream()
                .filter(entry -> {
                    Map<Long, Long> counts = countsByPosting.getOrDefault(entry.getKey(), Map.of());
                    return nextStageCalculator.isClosed(entry.getValue(), counts);
                })
                .map(entry -> String.valueOf(entry.getKey()))
                .toList();

        if (closedIds.isEmpty()) {
            log.info("마감 공고 없음");
            return;
        }
        redisTemplate.opsForZSet().remove(PostingViewTracker.VIEW_COUNT_KEY, closedIds.toArray());
        log.info("마감 공고 {}건 제거: {}", closedIds.size(), closedIds);
    }
}