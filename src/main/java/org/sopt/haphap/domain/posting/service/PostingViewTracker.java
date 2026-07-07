package org.sopt.haphap.domain.posting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostingViewTracker {

    public static final String VIEW_COUNT_KEY = "posting:view-count";

    private final RedisTemplate<String, String> redisTemplate;

    @Async
    public void recordView(Long postingId) {
        increment(postingId);
    }

    @Async
    public void recordCardClick(Long postingId) {
        increment(postingId);
    }

    private void increment(Long postingId) {
        try {
            redisTemplate.opsForZSet().incrementScore(VIEW_COUNT_KEY, postingId.toString(), 1);
        } catch (Exception e) {
            log.warn("조회수 집계 실패 - postingId={}, error={}", postingId, e.getMessage());
        }
    }
}