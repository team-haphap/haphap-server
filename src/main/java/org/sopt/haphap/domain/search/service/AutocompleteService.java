package org.sopt.haphap.domain.search.service;

import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.search.dto.AutocompleteRelatedKeywordResponse;
import org.sopt.haphap.domain.search.dto.AutocompleteResponse;
import org.sopt.haphap.domain.search.dto.AutocompleteShortcutResponse;
import org.sopt.haphap.domain.search.repository.RelatedSearchKeywordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutocompleteService {

    private static final int SHORTCUT_LIMIT = 5;
    private static final int JOB_LIMIT = 10;
    private static final Pattern INCOMPLETE_JAMO_ONLY = Pattern.compile("^[ㄱ-ㅣ]+$");

    private final PostingRepository postingRepository;
    private final HighlightRangeCalculator highlightRangeCalculator;
    private final RelatedSearchKeywordRepository relatedSearchKeywordRepository;

    public AutocompleteResponse autocomplete(String rawKeyword) {
        String keyword = normalize(rawKeyword);
        if (keyword == null) {
            return AutocompleteResponse.from(List.of(), List.of());
        }

        List<AutocompleteShortcutResponse> shortcuts = searchShortcuts(keyword);
        List<AutocompleteRelatedKeywordResponse> relatedKeywords = searchRelatedKeywords(keyword);

        return AutocompleteResponse.from(shortcuts, relatedKeywords);
    }

    private String normalize(String rawKeyword) {
        if (rawKeyword == null) {
            return null;
        }
        String trimmed = rawKeyword.trim();
        if (trimmed.isEmpty() || INCOMPLETE_JAMO_ONLY.matcher(trimmed).matches()) return null;
        return trimmed;
    }

    // 바로가기: 공고명(title) 매칭, 클릭 시 해당 공고 상세로 이동
    private List<AutocompleteShortcutResponse> searchShortcuts(String keyword) {
        return postingRepository.searchByTitleContaining(keyword, SHORTCUT_LIMIT).stream()
                .map(p -> new AutocompleteShortcutResponse(
                        p.getId(), p.getTitle(), p.getLogoImageUrl(),
                        highlightRangeCalculator.calculate(p.getTitle(), keyword)))
                .toList();
    }

    // 관련 검색어: 클릭 시 relatedKeywordId로 필터링된 목록 화면으로 이동
    private List<AutocompleteRelatedKeywordResponse> searchRelatedKeywords(String keyword) {
        return relatedSearchKeywordRepository.searchByKeywordContaining(keyword, JOB_LIMIT).stream()
                .map(k -> new AutocompleteRelatedKeywordResponse(
                        k.getId(), k.getKeyword(),
                        highlightRangeCalculator.calculate(k.getKeyword(), keyword)))
                .toList();
    }
}