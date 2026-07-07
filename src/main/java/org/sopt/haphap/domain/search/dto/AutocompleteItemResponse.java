package org.sopt.haphap.domain.search.dto;

import java.util.List;

public record AutocompleteItemResponse(
        AutocompleteType type,
        String name,
        List<HighlightRange> highlightRanges,
        Long postingId
) {
    public static AutocompleteItemResponse company(Long id, String name, List<HighlightRange> ranges) {
        return new AutocompleteItemResponse(AutocompleteType.COMPANY, name, ranges, id);
    }

    public static AutocompleteItemResponse job(Long id, String name, List<HighlightRange> ranges) {
        return new AutocompleteItemResponse(AutocompleteType.JOB, name, ranges, id);
    }
}

