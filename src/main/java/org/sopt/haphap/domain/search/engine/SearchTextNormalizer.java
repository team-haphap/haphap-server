package org.sopt.haphap.domain.search.engine;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class SearchTextNormalizer {

    private SearchTextNormalizer() {}

    /* 소문자 + 모든 공백 제거 */
    public static String normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
    }

    /* 공백 기준으로 단어 분리 후 각 단어 정규화 (앞 공백은 trim 으로 제거) */
    public static List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return Arrays.stream(text.trim().split("\\s+"))
                .map(SearchTextNormalizer::normalize)
                .filter(t -> !t.isEmpty())
                .distinct()
                .toList();
    }
}