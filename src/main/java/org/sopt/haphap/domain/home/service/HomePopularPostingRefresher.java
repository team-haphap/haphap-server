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
 * 매 정각, 직전 59분간의 home:popular:hour:{yyyyMMddHH} 버킷 전체를 읽어 home:popular:cache(ZSET)를 갱신.
 * 점수(조회수)를 그대로 옮겨 저장해, 읽기 단(HomePopularPostingService)에서
 * "조회수 동일 시 공고명 가나다순" 동점 처리
 * 상위 N개만 잘라서 캐싱하지 않고 버킷 전체를 옮기는 이유: 카테고리 필터가 있어서, 전역 상위 N개만
 * 캐싱하면 특정 카테고리의 실제 인기 공고가 그 N위 밖으로 밀려나 있을 때 해당 카테고리 결과가
 * 비거나 불완전해질 수 있다. 이 버킷 크기는 "그 시간대에 실제로 조회된 서로 다른 공고 수"로
 * 자연히 제한되므로(카탈로그 전체 크기가 사실상의 상한), 별도 캡을 두지 않아도 된다.
 * 직전 시간대에 조회 기록이 전혀 없으면 갱신하지 않고 기존 캐시를 그대로 유지한다(빈 상태 노출 방지).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HomePopularPostingRefresher {

    public static final String CACHE_KEY = "home:popular:cache";

    private final RedisTemplate<String, String> redisTemplate;

    @Scheduled(cron = "0 0 * * * *")
    public void refresh() {
        String previousHourBucket = HomePopularViewTracker.bucketKey(LocalDateTime.now().minusHours(1));
        Set<ZSetOperations.TypedTuple<String>> topPostings = redisTemplate.opsForZSet()
                .reverseRangeWithScores(previousHourBucket, 0, -1);

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
