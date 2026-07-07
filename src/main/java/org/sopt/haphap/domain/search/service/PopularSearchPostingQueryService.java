package org.sopt.haphap.domain.search.service;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.dto.response.PopularPostingListResponse;
import org.sopt.haphap.domain.posting.dto.response.PopularPostingResponse;
import org.sopt.haphap.domain.posting.service.support.PostingAggregate;
import org.sopt.haphap.domain.posting.service.support.PostingAggregateLoader;
import org.sopt.haphap.domain.posting.service.support.PostingResponseAssembler;
import org.sopt.haphap.domain.posting.service.support.PostingResponseAssembler.Scored;
import org.sopt.haphap.domain.posting.service.PostingViewTracker;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopularSearchPostingQueryService {

    private static final int POPULAR_COUNT = 4;

    private final RedisTemplate<String, String> redisTemplate;
    private final PostingAggregateLoader aggregateLoader;
    private final PostingResponseAssembler assembler;

    public PopularPostingListResponse getPopularPostings() {
        List<Long> topIds = fetchTopPostingIds();
        if (topIds.isEmpty()) {
            return PopularPostingListResponse.from(List.of());
        }

        PostingAggregate agg = aggregateLoader.load(topIds);

        List<PopularPostingResponse> responses = topIds.stream()
                .filter(id -> agg.posting(id) != null)
                .map(id -> assembler.assemble(agg.posting(id), agg.stages(id), agg.counts(id)))
                .map(Scored::response)
                .toList();

        return PopularPostingListResponse.from(responses);
    }

    private List<Long> fetchTopPostingIds() {
        Set<String> ids = redisTemplate.opsForZSet()
                .reverseRange(PostingViewTracker.VIEW_COUNT_KEY, 0, POPULAR_COUNT - 1);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream().map(Long::valueOf).toList();
    }
}