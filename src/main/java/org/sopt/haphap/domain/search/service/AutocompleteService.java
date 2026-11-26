package org.sopt.haphap.domain.search.service;

import java.text.Collator;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.posting.dto.projection.PostingAutocompleteProjection;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.search.dto.AutocompleteRelatedKeywordResponse;
import org.sopt.haphap.domain.search.dto.AutocompleteResponse;
import org.sopt.haphap.domain.search.dto.AutocompleteRelatedPostingResponse;
import org.sopt.haphap.domain.search.dto.RelatedSearchKeywordProjection;
import org.sopt.haphap.domain.search.engine.*;
import org.sopt.haphap.domain.search.repository.RelatedSearchKeywordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutocompleteService {

    private static final int RELATED_POSTING_LIMIT = 5;
    private static final int JOB_LIMIT = 10;
    private static final Pattern INCOMPLETE_JAMO_ONLY = Pattern.compile("^[ㄱ-ㅣ]+$");

    private static final Collator KOREAN = Collator.getInstance(Locale.KOREAN);

    private final PostingRepository postingRepository;
    private final HighlightRangeCalculator highlightRangeCalculator;
    private final RelatedSearchKeywordRepository relatedSearchKeywordRepository;
    private final PostingSearchEngine postingSearchEngine;
    private final SearchTermExpander termExpander;

    public AutocompleteResponse autocomplete(String rawKeyword) {
        String keyword = normalize(rawKeyword);
        if (keyword == null) {
            return AutocompleteResponse.from(List.of(), List.of());
        }
        return AutocompleteResponse.from(
                safeRelatedPostings(keyword),   // ES 장애 시 바로가기 영역만 비움
                safeRelatedKeywords(keyword));  // DB 장애 시 관련 검색어 영역만 비움
    }

    private List<AutocompleteRelatedPostingResponse> safeRelatedPostings(String keyword) {
        try {
            return searchRelatedPostings(keyword);
        } catch (Exception e) {
            log.warn("[자동완성] 공고 바로가기 조회 실패 - 영역 비움", e);
            return List.of();
        }
    }

    private List<AutocompleteRelatedKeywordResponse> safeRelatedKeywords(String keyword) {
        try {
            return searchRelatedKeywords(keyword);
        } catch (Exception e) {
            log.warn("[자동완성] 관련 검색어 조회 실패 - 영역 비움", e);
            return List.of();
        }
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

    /// 관련 검색어: 동의어(약칭·한영)·띄어쓰기 변형·대소문자 무시로 후보를 모으고
    // 완전일치 → 직접 일치 글자 수 내림차순 → 가나다순으로 정렬

    private List<AutocompleteRelatedKeywordResponse> searchRelatedKeywords(String keyword) {
        String normalizedInput = SearchTextNormalizer.normalize(keyword);
        if (normalizedInput.isEmpty()) {
            return List.of();
        }

        // 입력값 + 동의어 그룹 전체로 후보 수집 (중복은 id로 제거)
        Map<Long, RelatedSearchKeywordProjection> candidates = new LinkedHashMap<>();
        for (String alternative : termExpander.wholeAlternatives(keyword)) {
            relatedSearchKeywordRepository.searchByNormalizedContaining(alternative)
                    .forEach(k -> candidates.putIfAbsent(k.getId(), k));
        }
        if (candidates.isEmpty()) {
            return List.of();
        }

        String lowerInput = keyword.toLowerCase(Locale.KOREAN);

        Comparator<RelatedSearchKeywordProjection> order = Comparator
                .comparing((RelatedSearchKeywordProjection k) ->
                        !SearchTextNormalizer.normalize(k.getKeyword()).equals(normalizedInput))
                .thenComparing(k -> -directMatchLength(k.getKeyword(), lowerInput))
                .thenComparing(RelatedSearchKeywordProjection::getKeyword, KOREAN);

        return candidates.values().stream()
                .sorted(order)
                .limit(JOB_LIMIT)
                .map(k -> new AutocompleteRelatedKeywordResponse(
                        k.getId(), k.getKeyword(),
                        highlightRangeCalculator.calculate(k.getKeyword(), keyword)))
                .toList();
    }

    // 후보 텍스트에 입력값이 그대로 들어있으면 그 길이, 아니면 0 (동의어로만 매칭된 경우)
    private int directMatchLength(String candidate, String lowerInput) {
        return candidate.toLowerCase(Locale.KOREAN).contains(lowerInput) ? lowerInput.length() : 0;
    }
}

//(1) 마감공고 제외 (2) 완전일치 > 기업명 > 가나다 > 공고명 가나다 >  등록일 내림차순 > ID 오름차순
//(3) 후보는 from ES