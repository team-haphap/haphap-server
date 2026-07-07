package org.sopt.haphap.domain.search.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.search.dto.AutocompleteItemResponse;
import org.sopt.haphap.domain.search.dto.AutocompleteResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutocompleteService {

    private static final int JOB_LIMIT = 10;// 추후 관련 검색어..
    private static final int SHORTCUT_LIMIT = 5;

    private static final Pattern INCOMPLETE_JAMO_ONLY = Pattern.compile("^[ㄱ-ㅣ]+$");

    private final CompanyRepository companyRepository;
    private final PostingRepository postingRepository;
    private final HighlightRangeCalculator highlightRangeCalculator;

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

    // 바로가기: 공고명(title) 매칭, 클릭 시 해당 공고 상세로 이동 → postingId 필수
    private List<AutocompleteItemResponse> searchCompanies(String keyword) {
        return postingRepository.searchByTitleContaining(keyword, SHORTCUT_LIMIT).stream()
                .map(p -> AutocompleteItemResponse.company(
                        p.getId(), p.getTitle(),
                        highlightRangeCalculator.calculate(p.getTitle(), keyword)))
                .toList();
    }

    // 관련 검색어: 클릭 시 목록으로 이동, 특정 공고로 안 감 → postingId 항상 null
    // TODO: 사전 저장된 관련 검색어 테이블로 교체 예정. 지금은 임시로 공고명 매칭 재사용.
    private List<AutocompleteItemResponse> searchJobs(String keyword) {
        return postingRepository.searchByTitleContaining(keyword, JOB_LIMIT).stream()
                .map(p -> AutocompleteItemResponse.job(
                        null, p.getTitle(),
                        highlightRangeCalculator.calculate(p.getTitle(), keyword)))
                .toList();
    }
}