package org.sopt.haphap.domain.search.dto;

import java.util.Arrays;
import java.util.List;

public record PostingSearchCondition(
        String keyword,
        List<String> categories,
        int page,
        int size
) {
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    public static PostingSearchCondition of(
            String q, String category, Integer page, Integer size
    ) {
        int normalizedPage = (page == null || page < 0) ? 0 : page;
        int normalizedSize = (size == null || size <= 0) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        String normalizedKeyword = (q == null || q.isBlank()) ? null : q.trim();
        List<String> normalizedCategories = parseCategories(category);   // ← 이 계산 결과를 아래 생성자에 그대로 써야 함
        return new PostingSearchCondition(normalizedKeyword, normalizedCategories, normalizedPage, normalizedSize);
    }
    private static List<String> parseCategories(String category) {
        if (category == null || category.isBlank()) return null;
        List<String> result = Arrays.stream(category.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
        return result.isEmpty() ? null : result;
    }
}