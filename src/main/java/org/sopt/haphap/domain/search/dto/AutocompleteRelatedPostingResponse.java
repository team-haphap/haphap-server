package org.sopt.haphap.domain.search.dto;

import java.util.List;

public record AutocompleteRelatedPostingResponse(
        Long postingId,
        String name,
        String imageUrl,
        List<HighlightRange> highlightRanges
) {
}