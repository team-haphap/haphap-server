package org.sopt.haphap.domain.home.service;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 매 정각, 직전 59분간의 home:popular:hour:{yyyyMMddHH} 버킷을 읽어 home:popular:cache(ZSET)를 갱신.
 * 점수(조회수)를 그대로 옮겨 저장해, 읽기 단(HomePopularPostingService)에서
 * "조회수 동일 시 공고명 가나다순" 동점 처리
 * 직전 시간대에 조회 기록이 전혀 없으면 갱신하지 않고 기존 캐시를 그대로 유지한다(빈 상태 노출 방지).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HomePopularPostingRefresher {

    public static final String CACHE_KEY = "home:popular:cache";
    private static final int CANDIDATE_COUNT = 50;

    private final RedisTemplate<String, String> redisTemplate;

    @Scheduled(cron = "0 0 * * * *")
    public void refresh() {
        String previousHourBucket = HomePopularViewTracker.bucketKey(LocalDateTime.now().minusHours(1));
        Set<ZSetOperations.TypedTuple<String>> topPostings = redisTemplate.opsForZSet()
                .reverseRangeWithScores(previousHourBucket, 0, CANDIDATE_COUNT - 1);

        if (topPostings == null || topPostings.isEmpty()) {
            log.info("홈 인기 공고(지금많이보는 공고): 직전 시간대 조회 데이터 없음 - 기존 캐시 유지");
            return;
        }

        String tempKey = CACHE_KEY + ":tmp";
        redisTemplate.delete(tempKey);
        redisTemplate.opsForZSet().add(tempKey, topPostings);
        redisTemplate.rename(tempKey, CACHE_KEY);
        log.info("홈 인기 공고(지금많이보는 공고) 캐시 갱신 완료 - {}건", topPostings.size());
    }
}
