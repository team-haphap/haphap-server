package org.sopt.haphap.domain.home.service;

import java.text.Collator;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.home.dto.response.RecentViewListResponse;
import org.sopt.haphap.domain.home.dto.response.RecentViewResponse;
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
 * home:popular:cache(HomePopularPostingRefresher가 매 정각 갱신하는 ZSET)를 읽어 응답을 조립한다.
 * 정렬: 조회수(score) 내림차순, 동점이면 공고명 가나다순 - 우선순위는 숫자 → 한글 → 영문 → 그 외.
 * 팀원의 posting:popular-cache와는 완전히 별도의 캐시 - 조회/집계/갱신 전 과정이 독립적.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomePopularPostingService {

    private static final int MAX_POPULAR = 10;
    private static final Collator TITLE_COLLATOR = Collator.getInstance(Locale.KOREAN);
    private static final Comparator<String> TITLE_COMPARATOR = HomePopularPostingService::compareTitle;

    private final RedisTemplate<String, String> redisTemplate;
    private final PostingAggregateLoader aggregateLoader;
    private final CategoryParser categoryParser;
    private final HomeCardAssembler homeCardAssembler;

    public RecentViewListResponse getPopularPostings(List<String> category) {
        List<String> categories = categoryParser.parse(category);

        Map<Long, Double> scoreByPostingId = fetchCandidateScores();
        if (scoreByPostingId.isEmpty()) {
            return RecentViewListResponse.from(List.of());
        }

        List<Long> candidateIds = List.copyOf(scoreByPostingId.keySet());
        PostingAggregate agg = aggregateLoader.load(candidateIds, CompanyImageType.POPULAR);

        List<RecentViewResponse> result = candidateIds.stream()
                .map(agg::posting)
                .filter(Objects::nonNull)
                .filter(posting -> matchesCategory(posting, categories))
                .sorted(Comparator
                        .comparingDouble((Posting posting) -> scoreByPostingId.get(posting.getId())).reversed()
                        .thenComparing(Posting::getTitle, TITLE_COMPARATOR))
                .limit(MAX_POPULAR)
                .map(posting -> homeCardAssembler.assemble(posting, agg.companyImageUrl(posting.getId())))
                .toList();

        return RecentViewListResponse.from(result);
    }

    private boolean matchesCategory(Posting posting, List<String> categories) {
        return categories == null || categories.contains(posting.getCategory().getName());
    }

    private Map<Long, Double> fetchCandidateScores() {
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(HomePopularPostingRefresher.CACHE_KEY, 0, -1);
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

    private static int compareTitle(String a, String b) {
        TitleGroup groupA = TitleGroup.of(firstChar(a));
        TitleGroup groupB = TitleGroup.of(firstChar(b));
        if (groupA != groupB) {
            return Integer.compare(groupA.ordinal(), groupB.ordinal());
        }
        return TITLE_COLLATOR.compare(a, b);
    }

    private static char firstChar(String value) {
        return (value == null || value.isEmpty()) ? Character.MAX_VALUE : value.charAt(0);
    }

    // 동점 정렬 우선순위: 숫자 → 한글 → 영문 → 그 외
    private enum TitleGroup {
        DIGIT, HANGUL, LATIN, OTHER;

        static TitleGroup of(char c) {
            if (Character.isDigit(c)) {
                return DIGIT;
            }
            if (isHangul(c)) {
                return HANGUL;
            }
            if (isLatin(c)) {
                return LATIN;
            }
            return OTHER;
        }

        private static boolean isHangul(char c) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
            return block == Character.UnicodeBlock.HANGUL_SYLLABLES
                    || block == Character.UnicodeBlock.HANGUL_JAMO
                    || block == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
                    || block == Character.UnicodeBlock.HANGUL_JAMO_EXTENDED_A
                    || block == Character.UnicodeBlock.HANGUL_JAMO_EXTENDED_B;
        }

        private static boolean isLatin(char c) {
            return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
        }
    }
}
