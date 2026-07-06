package org.sopt.haphap.domain.posting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PostingViewTracker {

    public static final String VIEW_COUNT_KEY = "posting:view-count";

    private final RedisTemplate<String, String> redisTemplate;

    // 상세페이지 진입
    public void recordView(Long postingId) {
        increment(postingId);
    }

    // 추가한 거 - 카드 클릭
    public void recordCardClick(Long postingId) {
        increment(postingId);
    }

    private void increment(Long postingId) {
        redisTemplate.opsForZSet().incrementScore(VIEW_COUNT_KEY, postingId.toString(), 1);
    }

    // postingId 수만큼 Redis 왕복하지 않도록 파이프라인으로 한 번에 조회
    public Map<Long, Long> getViewCounts(List<Long> postingIds) {
        if (postingIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> counts = new HashMap<>();
        for (Long id : postingIds) {
            Double score = redisTemplate.opsForZSet().score(VIEW_COUNT_KEY, id.toString());
            counts.put(id, score == null ? 0L : score.longValue());
        }
        return counts;
    }
}