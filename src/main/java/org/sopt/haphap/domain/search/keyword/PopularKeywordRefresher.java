package org.sopt.haphap.domain.search.keyword;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PopularKeywordRefresher {

    public static final String POPULAR_KEYWORD_CACHE_KEY = "search:popular:keywords";
    private static final String TEMP_KEY = POPULAR_KEYWORD_CACHE_KEY + ":tmp";
    private static final int TOP_COUNT = 10;
    private static final int FETCH_COUNT = 1000;

    private final SearchKeywordLogRepository repository;
    private final RedisTemplate<String, String> redisTemplate;

    @Scheduled(cron = "0 0 * * * *")
    public void refresh() {
        try {
            LocalDateTime to = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
            LocalDateTime from = to.minusHours(1);

            List<KeywordCountProjection> rows = repository.aggregate(from, to, PageRequest.of(0, FETCH_COUNT));
            List<String> top = pickTop(rows);

            if (top.isEmpty()) {
                redisTemplate.delete(POPULAR_KEYWORD_CACHE_KEY);
                return;
            }
            redisTemplate.delete(TEMP_KEY);
            redisTemplate.opsForList().rightPushAll(TEMP_KEY, top);
            redisTemplate.rename(TEMP_KEY, POPULAR_KEYWORD_CACHE_KEY);
        } catch (Exception e) {
            log.error("[인기 검색어] 갱신 실패 - 이전 목록 유지", e);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initIfAbsent() {
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(POPULAR_KEYWORD_CACHE_KEY))) {
            refresh();
        }
    }

    // 횟수 내림차순, 동일 횟수끼리는 랜덤 순서로 섞은 뒤 상위 10개 -> TODO.기획 문서 확인해보기
    private List<String> pickTop(List<KeywordCountProjection> rows) {
        Map<Long, List<String>> byCount = new LinkedHashMap<>();
        for (KeywordCountProjection row : rows) {
            byCount.computeIfAbsent(row.getCnt(), k -> new ArrayList<>()).add(row.getKeyword());
        }
        List<String> result = new ArrayList<>();
        for (List<String> sameCount : byCount.values()) {
            Collections.shuffle(sameCount);
            for (String keyword : sameCount) {
                if (result.size() == TOP_COUNT) {
                    return result;
                }
                result.add(keyword);
            }
        }
        return result;
    }
}