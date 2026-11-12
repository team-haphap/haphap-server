package org.sopt.haphap.domain.search.keyword;

import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.search.dto.PopularKeywordListResponse;
import org.sopt.haphap.domain.search.dto.PopularKeywordResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PopularSearchKeywordQueryService {

    private final RedisTemplate<String, String> redisTemplate;

    public PopularKeywordListResponse getPopularKeywords() {
        List<String> keywords = redisTemplate.opsForList()
                .range(PopularKeywordRefresher.POPULAR_KEYWORD_CACHE_KEY, 0, -1);
        if (keywords == null || keywords.isEmpty()) {
            return PopularKeywordListResponse.from(List.of());
        }
        List<PopularKeywordResponse> responses = IntStream.range(0, keywords.size())
                .mapToObj(i -> new PopularKeywordResponse(i + 1, keywords.get(i)))
                .toList();
        return PopularKeywordListResponse.from(responses);
    }
}