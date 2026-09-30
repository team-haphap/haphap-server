package org.sopt.haphap.domain.home.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.home.dto.response.RecentViewListResponse;
import org.sopt.haphap.domain.posting.domain.CompanyImageType;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.service.support.CategoryParser;
import org.sopt.haphap.domain.posting.service.support.PostingAggregate;
import org.sopt.haphap.domain.posting.service.support.PostingAggregateLoader;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 홈 [지금 많이 보는 공고]: 최대 10개, [전체]/카테고리(다중 선택) 필터, 마감된 공고도 노출 유지.
 * HomePopularPostingRefresher가 매 정각 카테고리별로 미리 순위를 매겨 캐싱해둔 결과(home:popular:cache:*,
 * ZSET)를 읽기만 한다 - 정렬/카테고리 필터링을 요청마다 다시 하지 않는다. 여러 카테고리를 선택하면
 * 각 카테고리 캐시를 합친 뒤 다시 정렬해서 상위 10개를 뽑는다.
 * 정렬: 조회수(score) 내림차순, 동점이면 공고명 가나다순 - 우선순위는 숫자 → 한글 → 영문 → 그 외.
 * 팀원의 posting:popular-cache와는 완전히 별도의 캐시 - 조회/집계/갱신 전 과정이 독립적.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomePopularPostingService {

    private static final int MAX_POPULAR = 10;

    private final RedisTemplate<String, String> redisTemplate;
    private final PostingAggregateLoader aggregateLoader;
    private final CategoryParser categoryParser;
    private final HomeCardAssembler homeCardAssembler;

    public RecentViewListResponse getPopularPostings(List<String> category) {
        List<String> categories = categoryParser.parse(category);

        Map<Long, Double> scoreByPostingId = categories == null
                ? fetchScores(HomePopularPostingRefresher.cacheKeyFor(null))
                : mergeScores(categories);
        if (scoreByPostingId.isEmpty()) {
            return RecentViewListResponse.from(List.of());
        }

        List<Long> candidateIds = List.copyOf(scoreByPostingId.keySet());
        PostingAggregate agg = aggregateLoader.load(candidateIds, CompanyImageType.POPULAR);

        List<Posting> postings = candidateIds.stream()
                .map(agg::posting)
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparingDouble((Posting posting) -> scoreByPostingId.get(posting.getId())).reversed()
                        .thenComparing(Posting::getTitle, PopularPostingTitleTieBreaker.COMPARATOR))
                .limit(MAX_POPULAR)
                .toList();

        return RecentViewListResponse.from(homeCardAssembler.assemble(postings, agg));
    }

    private Map<Long, Double> mergeScores(List<String> categories) {
        Map<Long, Double> merged = new LinkedHashMap<>();
        for (String category : categories) {
            merged.putAll(fetchScores(HomePopularPostingRefresher.cacheKeyFor(category)));
        }
        return merged;
    }

    private Map<Long, Double> fetchScores(String cacheKey) {
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(cacheKey, 0, -1);
        if (tuples == null || tuples.isEmpty()) {
            return Map.of();
        }

        Map<Long, Double> scoreByPostingId = new LinkedHashMap<>();
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            if (tuple.getValue() == null || tuple.getScore() == null) {
                continue;
            }
            scoreByPostingId.put(Long.valueOf(tuple.getValue()), tuple.getScore());
        }
        return scoreByPostingId;
    }
}
