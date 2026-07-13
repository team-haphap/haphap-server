package org.sopt.haphap.domain.search.dto;

import java.util.List;

public record AutocompleteResponse(
        List<AutocompleteRelatedPostingResponse> relatedPostings,   // shortcuts 에서 relatedPostings
        List<AutocompleteRelatedKeywordResponse> relatedKeywords
) {
    public static AutocompleteResponse from(
            List<AutocompleteRelatedPostingResponse> relatedPostings,
            List<AutocompleteRelatedKeywordResponse> relatedKeywords
    ) {
        return new AutocompleteResponse(relatedPostings, relatedKeywords);
    }
}