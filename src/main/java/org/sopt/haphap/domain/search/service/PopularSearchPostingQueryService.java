package org.sopt.haphap.domain.search.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.service.PostingViewTracker;
import org.sopt.haphap.domain.search.dto.PopularSearchPostingListResponse;
import org.sopt.haphap.domain.search.dto.PopularSearchPostingResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopularSearchPostingQueryService {

    private static final int POPULAR_COUNT = 4;

    private final RedisTemplate<String, String> redisTemplate;
    private final PostingRepository postingRepository;

    // 캐시 없이, 호출될 때마다 그 순간 기준으로 계산 (실시간 트래킹)
    public PopularSearchPostingListResponse getPopularPostings() {
        List<Long> topIds = fetchTopPostingIds();
        if (topIds.isEmpty()) {
            return PopularSearchPostingListResponse.from(List.of());
        }
        return PopularSearchPostingListResponse.from(buildResponses(topIds));
    }

    private List<Long> fetchTopPostingIds() {
        Set<String> ids = redisTemplate.opsForZSet()
                .reverseRange(PostingViewTracker.VIEW_COUNT_KEY, 0, POPULAR_COUNT - 1);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream().map(Long::valueOf).toList();
    }

    private List<PopularSearchPostingResponse> buildResponses(List<Long> orderedIds) {
        Map<Long, Posting> postingMap = postingRepository
                .findAllWithCompanyAndCategoryByIds(orderedIds).stream()
                .collect(Collectors.toMap(Posting::getId, Function.identity()));

        return orderedIds.stream()
                .map(postingMap::get)
                .filter(Objects::nonNull)
                .map(this::toResponse)
                .toList();
    }

    private PopularSearchPostingResponse toResponse(Posting posting) {
        LocalDate today = LocalDate.now();

        Integer dDay = (posting.getDeadline() == null)
                ? null
                : (int) ChronoUnit.DAYS.between(today, posting.getDeadline());

        return new PopularSearchPostingResponse(
                posting.getId(),
                posting.getCompany().getName(),
                posting.getTitle(),
                posting.getCategory().getName(),
                dDay
        );
    }
}