package org.sopt.haphap.domain.search.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.search.dto.PopularSearchPostingResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PopularSearchPostingCacheRepository {

    private static final String CACHE_KEY = "search:popular:postings";
    private static final long TTL_MINUTES = 15; // 10분 주기 + 여유분,,

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void save(List<PopularSearchPostingResponse> postings) {
        try {
            String json = objectMapper.writeValueAsString(postings);
            redisTemplate.opsForValue().set(CACHE_KEY, json, TTL_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("인기 공고 캐시 직렬화 실패", e);
        }
    }

    public List<PopularSearchPostingResponse> find() {
        String json = redisTemplate.opsForValue().get(CACHE_KEY);
        if (json == null) {
            return List.of();
        }
        try {
            CollectionType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, PopularSearchPostingResponse.class);
            return objectMapper.readValue(json, listType);
        } catch (JsonProcessingException e) {
            log.error("인기 공고 캐시 역직렬화 실패", e);
            return List.of();
        }
    }
}