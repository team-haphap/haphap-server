package org.sopt.haphap.domain.search.dto;

import java.util.List;

public record PostingSearchCondition(
        String keyword,
        List<String> categories,
        String status,
        int page,
        int size
) {
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    public static PostingSearchCondition of(
            String q, List<String> categories, String status, Integer page, Integer size
    ) {
        int normalizedPage = (page == null || page < 0) ? 0 : page;
        int normalizedSize = (size == null || size <= 0) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        String normalizedKeyword = (q == null || q.isBlank()) ? null : q.trim();
        return new PostingSearchCondition(normalizedKeyword, categories, status, normalizedPage, normalizedSize);
    }
}