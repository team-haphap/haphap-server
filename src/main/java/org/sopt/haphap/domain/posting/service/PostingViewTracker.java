package org.sopt.haphap.domain.posting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostingViewTracker {

    public static final String VIEW_COUNT_KEY = "posting:view-count";

    private final RedisTemplate<String, String> redisTemplate;

    public void recordView(Long postingId) {
        redisTemplate.opsForZSet().incrementScore(VIEW_COUNT_KEY, postingId.toString(), 1);
    }
}