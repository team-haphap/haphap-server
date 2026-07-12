package org.sopt.haphap.domain.search.dto;

import java.util.List;

public record AutocompleteItemResponse(
        AutocompleteType type,
        String name,
        List<HighlightRange> highlightRanges,
        Long postingId,
        String imageUrl,
        Long keywordId
) {
    public static AutocompleteItemResponse company(Long postingId, String name, List<HighlightRange> ranges, String imageUrl) {
        return new AutocompleteItemResponse(AutocompleteType.COMPANY, name, ranges, postingId, imageUrl, null);
    }
    public static AutocompleteItemResponse job(Long keywordId, String name, List<HighlightRange> ranges) {
        return new AutocompleteItemResponse(AutocompleteType.KEYWORD, name, ranges, null, null, keywordId);
    }
}