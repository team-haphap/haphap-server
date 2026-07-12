package org.sopt.haphap.domain.search.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.search.dto.AutocompleteItemResponse;
import org.sopt.haphap.domain.search.dto.AutocompleteResponse;
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
            return AutocompleteResponse.from(List.of());
        }

        List<AutocompleteItemResponse> results = new ArrayList<>();
        results.addAll(searchCompanies(keyword));
        results.addAll(searchJobs(keyword));

        return AutocompleteResponse.from(results);
    }

    private String normalize(String rawKeyword) {
        if (rawKeyword == null) {
            return null;
        }
        String trimmed = rawKeyword.trim();
        if (trimmed.isEmpty() || INCOMPLETE_JAMO_ONLY.matcher(trimmed).matches()) return null;
        return trimmed;
    }

    private List<AutocompleteItemResponse> searchCompanies(String keyword) {
        return postingRepository.searchByTitleContaining(keyword, SHORTCUT_LIMIT).stream()
                .map(p -> AutocompleteItemResponse.company(
                        p.getId(), p.getTitle(),
                        highlightRangeCalculator.calculate(p.getTitle(), keyword),
                        p.getLogoImageUrl()))
                .toList();
    }

    private List<AutocompleteItemResponse> searchJobs(String keyword) {
        return relatedSearchKeywordRepository.searchByKeywordContaining(keyword, JOB_LIMIT).stream()
                .map(k -> AutocompleteItemResponse.job(
                        k.getId(), k.getKeyword(),
                        highlightRangeCalculator.calculate(k.getKeyword(), keyword)))
                .toList();
    }
}