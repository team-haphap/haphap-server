package org.sopt.haphap.domain.home.service;

import java.time.LocalDateTime;
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
    // 최종 응답은 상위 10개지만, Redis 자체 동점 처리(멤버 문자열 사전식)가 우리 정렬 규칙(제목 가나다순)과
    // 달라서 경계값 근처 동점자를 놓치지 않도록 여유를 두고 캐싱한다.
    private static final int TOP_N_PER_KEY = 20;

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
        Map<String, Map<Long, Double>> scoresByCategory = groupByCategory(scoreByPostingId);

        writeTopN(cacheKeyFor(null), scoreByPostingId);
        scoresByCategory.forEach((category, scores) -> writeTopN(cacheKeyFor(category), scores));

        log.info("홈 인기 공고 캐시 갱신 완료 - 전체 {}건, {}개 카테고리",
                scoreByPostingId.size(), scoresByCategory.size());
    }

    private Map<String, Map<Long, Double>> groupByCategory(Map<Long, Double> scoreByPostingId) {
        List<Long> postingIds = List.copyOf(scoreByPostingId.keySet());
        Map<Long, String> categoryByPostingId = postingRepository.findCategoryNamesByIds(postingIds).stream()
                .collect(Collectors.toMap(
                        PostingCategoryProjection::getPostingId, PostingCategoryProjection::getCategoryName));

        Map<String, Map<Long, Double>> scoresByCategory = new HashMap<>();
        scoreByPostingId.forEach((postingId, score) -> {
            String category = categoryByPostingId.get(postingId);
            if (category == null) {
                // 조회 이후 공고가 삭제되는 등의 예외적 상황 - 카테고리별 캐시에서만 제외, 전체 캐시엔 그대로 반영.
                return;
            }
            scoresByCategory.computeIfAbsent(category, key -> new HashMap<>()).put(postingId, score);
        });
        return scoresByCategory;
    }

    private void writeTopN(String key, Map<Long, Double> scoreByPostingId) {
        Set<TypedTuple<String>> topEntries = scoreByPostingId.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(TOP_N_PER_KEY)
                .map(entry -> (TypedTuple<String>) new DefaultTypedTuple<>(entry.getKey().toString(), entry.getValue()))
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
}
