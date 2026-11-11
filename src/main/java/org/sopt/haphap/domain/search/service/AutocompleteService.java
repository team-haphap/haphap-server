package org.sopt.haphap.domain.search.service;

import java.text.Collator;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.dto.projection.PostingAutocompleteProjection;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.search.dto.AutocompleteRelatedKeywordResponse;
import org.sopt.haphap.domain.search.dto.AutocompleteResponse;
import org.sopt.haphap.domain.search.dto.AutocompleteRelatedPostingResponse;
import org.sopt.haphap.domain.search.engine.PostingSearchEngine;
import org.sopt.haphap.domain.search.engine.PostingSearchHit;
import org.sopt.haphap.domain.search.engine.PostingSearchHitResult;
import org.sopt.haphap.domain.search.engine.SearchTextNormalizer;
import org.sopt.haphap.domain.search.repository.RelatedSearchKeywordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutocompleteService {

    private static final int RELATED_POSTING_LIMIT = 5;
    private static final int JOB_LIMIT = 10;
    private static final Pattern INCOMPLETE_JAMO_ONLY = Pattern.compile("^[ㄱ-ㅣ]+$");

    private static final int CANDIDATE_LIMIT = 200;
    private static final Collator KOREAN = Collator.getInstance(Locale.KOREAN);

    private final PostingRepository postingRepository;
    private final HighlightRangeCalculator highlightRangeCalculator;
    private final RelatedSearchKeywordRepository relatedSearchKeywordRepository;
    private final PostingSearchEngine postingSearchEngine;

    public AutocompleteResponse autocomplete(String rawKeyword) {
        String keyword = normalize(rawKeyword);
        if (keyword == null) {
            return AutocompleteResponse.from(List.of(), List.of());
        }
        return AutocompleteResponse.from(searchRelatedPostings(keyword), searchRelatedKeywords(keyword));
    }

    private String normalize(String rawKeyword) {
        if (rawKeyword == null) {
            return null;
        }
        String trimmed = rawKeyword.trim();
        if (trimmed.isEmpty() || INCOMPLETE_JAMO_ONLY.matcher(trimmed).matches()) return null;
        return trimmed;
    }

    // 바로가기: 기업명+공고명 매칭, 마감 공고 포함, 최대 5개
    private List<AutocompleteRelatedPostingResponse> searchRelatedPostings(String keyword) {
        PostingSearchHitResult result = postingSearchEngine.search(keyword, null);
        List<PostingSearchHit> hits = Stream.concat(result.all().stream(), result.partial().stream())
                .limit(CANDIDATE_LIMIT)
                .toList();
        if (hits.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> registeredAt = new HashMap<>();
        hits.forEach(h -> registeredAt.putIfAbsent(h.postingId(), h.registeredAtEpoch()));

        List<PostingAutocompleteProjection> rows =
                postingRepository.findAutocompleteRowsByIds(new ArrayList<>(registeredAt.keySet()));

        String normalizedKeyword = SearchTextNormalizer.normalize(keyword);

        Comparator<PostingAutocompleteProjection> order = Comparator
                .comparing((PostingAutocompleteProjection p) -> !isExact(p, normalizedKeyword))
                .thenComparing(PostingAutocompleteProjection::getCompanyName, KOREAN)
                .thenComparing(PostingAutocompleteProjection::getTitle, KOREAN)
                .thenComparing(p -> registeredAt.getOrDefault(p.getId(), 0L), Comparator.reverseOrder())
                .thenComparing(PostingAutocompleteProjection::getId);

        return rows.stream()
                .sorted(order)
                .limit(RELATED_POSTING_LIMIT)
                .map(p -> {
                    String displayName = p.getCompanyName() + " " + p.getTitle();
                    return new AutocompleteRelatedPostingResponse(
                            p.getId(), displayName, p.getLogoImageUrl(),
                            highlightRangeCalculator.calculate(displayName, keyword));
                })
                .toList();
    }

    // 기업명 또는 공고명 또는 "기업명 공고명"이 입력값과 (대소문자/공백 무시) 완전히 같은 경우
    private boolean isExact(PostingAutocompleteProjection p, String normalizedKeyword) {
        String company = SearchTextNormalizer.normalize(p.getCompanyName());
        String title = SearchTextNormalizer.normalize(p.getTitle());
        return company.equals(normalizedKeyword)
                || title.equals(normalizedKeyword)
                || (company + title).equals(normalizedKeyword);
    }

    // 관련 검색어: 기존 그대로 (DB related_search_keyword)
    private List<AutocompleteRelatedKeywordResponse> searchRelatedKeywords(String keyword) {
        return relatedSearchKeywordRepository.searchByKeywordContaining(keyword, JOB_LIMIT).stream()
                .map(k -> new AutocompleteRelatedKeywordResponse(
                        k.getId(), k.getKeyword(),
                        highlightRangeCalculator.calculate(k.getKeyword(), keyword)))
                .toList();
    }
}

//(1) 마감공고 제외 (2) 완전일치 > 기업명 > 가나다 > 공고명 가나다 >  등록일 내림차순 > ID 오름차순
//(3) 후보는 from ES