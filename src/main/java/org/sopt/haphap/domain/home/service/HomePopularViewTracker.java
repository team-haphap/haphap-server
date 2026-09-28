package org.sopt.haphap.domain.home.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 홈 [지금 많이 보는 공고] 전용 조회수 집계. 팀원의 posting:view-count(전체 누적 ZSET)와는
 * 완전히 별도의 키 공간을 사용 — 정각 기준 직전 59분 집계를 위해 시간대별 버킷으로 분리.
 * PostingViewTracker/PopularPostingCacheRefresher/PopularSearchPostingQueryService는 건드리지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HomePopularViewTracker {

    private static final String HOUR_BUCKET_PREFIX = "home:popular:hour:";
    private static final DateTimeFormatter HOUR_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHH");
    private static final Duration BUCKET_TTL = Duration.ofHours(2);

    private final RedisTemplate<String, String> redisTemplate;

    @Async
    public void record(Long postingId) {
        try {
            String key = bucketKey(LocalDateTime.now());
            redisTemplate.opsForZSet().incrementScore(key, postingId.toString(), 1);
            redisTemplate.expire(key, BUCKET_TTL);
        } catch (Exception e) {
            log.warn("홈 인기 공고 조회수 집계 실패 - postingId={}, error={}", postingId, e.getMessage());
        }
    }

    static String bucketKey(LocalDateTime time) {
        return HOUR_BUCKET_PREFIX + time.format(HOUR_FORMAT);
    }
}
