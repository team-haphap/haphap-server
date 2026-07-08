package org.sopt.haphap.domain.posting.service;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PopularPostingCacheRefresher {

    public static final String POPULAR_CACHE_KEY = "posting:popular-cache";
    private static final int POPULAR_COUNT = 4;

    private final RedisTemplate<String, String> redisTemplate;

    // 매시 정각: 누적 점수 기준 상위 N개를 스냅샷으로 캐시에 저장
    @Scheduled(cron = "0 0 * * * *")
    public void refresh() {
        Set<String> topIds = redisTemplate.opsForZSet()
                .reverseRange(PostingViewTracker.VIEW_COUNT_KEY, 0, POPULAR_COUNT - 1);

        String tempKey = POPULAR_CACHE_KEY + ":tmp";
        redisTemplate.delete(tempKey);

        if (topIds == null || topIds.isEmpty()) {
            redisTemplate.delete(POPULAR_CACHE_KEY);
            return;
        }
        redisTemplate.opsForList().rightPushAll(tempKey, topIds.toArray(new String[0]));
        redisTemplate.rename(tempKey, POPULAR_CACHE_KEY);
    }
}