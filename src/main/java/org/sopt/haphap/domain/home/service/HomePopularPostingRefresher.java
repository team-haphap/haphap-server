package org.sopt.haphap.domain.home.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.posting.dto.projection.PostingCategoryProjection;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 매 정각, 직전 59분간의 home:popular:hour:{yyyyMMddHH} 버킷 전체를 카테고리별로 나눠 미리 순위를 매겨 캐싱한다.
 *
 * 직전 시간대에 조회 기록이 전혀 없으면 아무 키도 갱신하지 않고 기존 캐시를 그대로 유지한다(빈 상태 노출 방지).
 * 이 규칙은 카테고리 단위로도 동일하게 적용된다 - 이번 시간대에 조회가 없었던 카테고리는 그 카테고리 키만
 * 건드리지 않고 넘어간다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HomePopularPostingRefresher {

    private static final String CACHE_KEY_PREFIX = "home:popular:cache:";
    private static final String ALL_CATEGORIES_KEY_SUFFIX = "ALL";

    private final RedisTemplate<String, String> redisTemplate;
    private final PostingRepository postingRepository;

    public static String cacheKeyFor(String categoryName) {
        return CACHE_KEY_PREFIX + (categoryName == null ? ALL_CATEGORIES_KEY_SUFFIX : categoryName);
    }

    @Scheduled(cron = "0 0 * * * *")
    public void refresh() {
        String previousHourBucket = HomePopularViewTracker.bucketKey(LocalDateTime.now().minusHours(1));
        Set<TypedTuple<String>> scored = redisTemplate.opsForZSet()
                .reverseRangeWithScores(previousHourBucket, 0, -1);

        if (scored == null || scored.isEmpty()) {
            log.info("홈 인기 공고: 직전 시간대 조회 데이터 없음 - 기존 캐시 유지");
            return;
        }

        Map<Long, Double> scoreByPostingId = toScoreMap(scored);
        List<Long> postingIds = List.copyOf(scoreByPostingId.keySet());

        // 삭제 등으로 더 이상 존재하지 않는 posting은 여기서 자연히 빠진다 - 어차피 나중에 응답 조립 시에도
        // 걸러질 대상이라, ALL/카테고리 캐시 모두에서 제외하는 게 맞다.
        List<PopularCandidate> candidates = postingRepository.findCategoryNamesByIds(postingIds).stream()
                .map(row -> new PopularCandidate(
                        row.getPostingId(), scoreByPostingId.get(row.getPostingId()), row.getCategoryName()))
                .toList();

        writeAboveThreshold(cacheKeyFor(null), candidates);

        Map<String, List<PopularCandidate>> byCategory = candidates.stream()
                .collect(Collectors.groupingBy(PopularCandidate::categoryName));
        byCategory.forEach((category, categoryCandidates) -> writeAboveThreshold(cacheKeyFor(category), categoryCandidates));

        log.info("홈 인기 공고 캐시 갱신 완료 - 전체 {}건, {}개 카테고리", candidates.size(), byCategory.size());
    }

    private void writeAboveThreshold(String key, List<PopularCandidate> candidates) {
        List<Double> scoresDesc = candidates.stream()
                .map(PopularCandidate::score)
                .sorted(Comparator.reverseOrder())
                .toList();
        double threshold = scoresDesc.size() >= HomePopularPostingService.MAX_POPULAR
                ? scoresDesc.get(HomePopularPostingService.MAX_POPULAR - 1)
                : Double.NEGATIVE_INFINITY;

        Set<TypedTuple<String>> topEntries = candidates.stream()
                .filter(c -> c.score() >= threshold)
                .map(c -> (TypedTuple<String>) new DefaultTypedTuple<>(c.postingId().toString(), c.score()))
                .collect(Collectors.toSet());

        String tempKey = key + ":tmp";
        redisTemplate.delete(tempKey);
        redisTemplate.opsForZSet().add(tempKey, topEntries);
        redisTemplate.rename(tempKey, key);
    }

    private Map<Long, Double> toScoreMap(Set<TypedTuple<String>> tuples) {
        Map<Long, Double> scoreByPostingId = new HashMap<>();
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            if (tuple.getValue() == null || tuple.getScore() == null) {
                continue;
            }
            scoreByPostingId.put(Long.valueOf(tuple.getValue()), tuple.getScore());
        }
        return scoreByPostingId;
    }

    private record PopularCandidate(Long postingId, Double score, String categoryName) {
    }
}
