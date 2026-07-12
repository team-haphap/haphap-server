package org.sopt.haphap.domain.search.dto;

import java.util.List;

public record AutocompleteResponse(
        List<AutocompleteShortcutResponse> shortcuts,
        List<AutocompleteRelatedKeywordResponse> relatedKeywords
) {
    public static AutocompleteResponse from(
            List<AutocompleteShortcutResponse> shortcuts,
            List<AutocompleteRelatedKeywordResponse> relatedKeywords
    ) {
        return new AutocompleteResponse(shortcuts, relatedKeywords);
    }
}