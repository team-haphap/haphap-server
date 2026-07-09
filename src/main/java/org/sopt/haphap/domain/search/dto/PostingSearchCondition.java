package org.sopt.haphap.domain.search.dto;

public record PostingSearchCondition(
        String keyword,
        String category,
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
        String normalizedCategory = (category == null || category.isBlank()) ? null : category.trim();
        return new PostingSearchCondition(normalizedKeyword, category, normalizedPage, normalizedSize);
    }
}