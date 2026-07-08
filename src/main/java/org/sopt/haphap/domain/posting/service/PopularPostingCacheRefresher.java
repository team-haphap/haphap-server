package org.sopt.haphap.domain.posting.service;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PopularPostingCacheRefresher {

    public static final String POPULAR_CACHE_KEY = "posting:popular-cache";
    private static final int POPULAR_COUNT = 4;

    private final RedisTemplate<String, String> redisTemplate;

    @Scheduled(cron = "0 0 * * * *")
    public void refresh() {
        Set<String> topIds = redisTemplate.opsForZSet()
                .reverseRange(PostingViewTracker.VIEW_COUNT_KEY, 0, POPULAR_COUNT - 1);

        String tempKey = POPULAR_CACHE_KEY + ":tmp";
        redisTemplate.delete(tempKey);

        if (topIds == null || topIds.isEmpty()) {
            redisTemplate.delete(POPULAR_CACHE_KEY);
            log.info("인기 공고 캐시 비움 (조회수 데이터 없음)");
            return;
        }
        redisTemplate.opsForList().rightPushAll(tempKey, topIds.toArray(new String[0]));
        redisTemplate.rename(tempKey, POPULAR_CACHE_KEY);
        log.info("인기 공고 캐시 갱신: {}", topIds);
    }
}