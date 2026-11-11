package org.sopt.haphap.domain.search.engine;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchTermExpander {

    private final SearchSynonymProperties properties;

    private Map<String, Set<String>> termToGroup = Map.of();

    @PostConstruct
    void init() {
        Map<String, Set<String>> map = new HashMap<>();
        for (List<String> group : properties.getSynonyms()) {
            Set<String> normalized = group.stream()
                    .map(SearchTextNormalizer::normalize)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            for (String term : normalized) {
                map.computeIfAbsent(term, k -> new LinkedHashSet<>()).addAll(normalized);
            }
        }
        this.termToGroup = map;
    }

    /*
     * 검색어를 "단어별 대체어 목록"으로 변환한다.
     * 전체 문자열이 동의어 사전에 있으면(예: "카카오 뱅크" → "카카오뱅크") 한 덩어리로 취급한다.
     *  기획측에 더 주면 더 추가하기 */

    public List<List<String>> expandTokens(String keyword) {
        String whole = SearchTextNormalizer.normalize(keyword);
        if (whole.isEmpty()) {
            return List.of();
        }
        if (termToGroup.containsKey(whole)) {
            return List.of(new ArrayList<>(termToGroup.get(whole)));
        }
        return SearchTextNormalizer.tokenize(keyword).stream()
                .map(this::alternatives)
                .toList();
    }

    private List<String> alternatives(String token) {
        Set<String> group = termToGroup.get(token);
        return group == null ? List.of(token) : new ArrayList<>(group);
    }
}