package org.sopt.haphap.domain.search.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.service.PostingViewTracker;
import org.sopt.haphap.domain.search.dto.PopularSearchPostingResponse;
import org.sopt.haphap.domain.search.repository.PopularSearchPostingCacheRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PopularSearchPostingCacheRefresher {

    private static final String REFRESH_LOCK_KEY = "lock:popular-search-refresh";
    private static final int POPULAR_COUNT = 4; // 가로 스크롤 최대 4개

    private final RedisTemplate<String, String> redisTemplate;
    private final PostingRepository postingRepository;
    private final PopularSearchPostingCacheRepository cacheRepository;

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.MINUTES)
    public void refresh() {
        // 블루그린 배포 중 두 인스턴스가 동시에 스케줄러를 돌릴 수 있어 짧은 분산락 -> 중복 실행 방지
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(REFRESH_LOCK_KEY, "1", 5, TimeUnit.SECONDS);
        if (locked == null || !locked) {
            log.info("다른 인스턴스가 이미 인기 공고 집계 중이라 스킵");
            return;
        }

        try {
            List<Long> topIds = fetchTopPostingIds();
            List<PopularSearchPostingResponse> result =
                    topIds.isEmpty() ? List.of() : buildResponses(topIds);
            cacheRepository.save(result);
            log.info("인기 공고 캐시 갱신 완료 - {}건", result.size());
        } catch (Exception e) {
            // 스케줄러는 실패해도 애플리케이션이 죽으면 안 되므로 여기서 흡수
            log.error("인기 공고 집계 실패", e);
        }
    }

    private List<Long> fetchTopPostingIds() {
        Set<String> ids = redisTemplate.opsForZSet()
                .reverseRange(PostingViewTracker.VIEW_COUNT_KEY, 0, POPULAR_COUNT - 1);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        // reverseRange는 순위 순서를 보존하는 LinkedHashSet을 반환
        return ids.stream().map(Long::valueOf).toList();
    }

    private List<PopularSearchPostingResponse> buildResponses(List<Long> orderedIds) {
        // N+1 방지: id들로 회사/카테고리까지 한 쿼리에 fetch join
        Map<Long, Posting> postingMap = postingRepository
                .findAllByIdInWithCompanyAndCategory(orderedIds).stream()
                .collect(Collectors.toMap(Posting::getId, Function.identity()));

        // Redis 랭킹 순서대로 재정렬하기
        return orderedIds.stream()
                .map(postingMap::get)
                .filter(Objects::nonNull) // 집계~조회 사이 삭제된 공고 방어
                .map(this::toResponse)
                .toList();
    }

    private PopularSearchPostingResponse toResponse(Posting posting) {
        Integer dDay = (posting.getDeadline() == null)
                ? null
                : (int) ChronoUnit.DAYS.between(LocalDate.now(), posting.getDeadline());
        return new PopularSearchPostingResponse(
                posting.getId(),
                posting.getCompany().getName(),
                posting.getTitle(),
                posting.getCategory().getName(),
                dDay
        );
    }
}