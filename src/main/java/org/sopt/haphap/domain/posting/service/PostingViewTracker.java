package org.sopt.haphap.domain.posting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

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
}