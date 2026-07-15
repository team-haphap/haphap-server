package org.sopt.haphap.domain.search.dto;

import java.util.List;

public record AutocompleteRelatedKeywordResponse(
        Long keywordId,
        String name,
        List<HighlightRange> highlightRanges
) {
}