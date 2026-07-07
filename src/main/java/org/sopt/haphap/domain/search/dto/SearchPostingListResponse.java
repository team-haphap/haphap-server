package org.sopt.haphap.domain.search.dto;

import java.util.List;

public record SearchPostingListResponse(
        List<SearchPostingResponse> postings,
        int page,
        int size,
        boolean hasNext
) {
    public static SearchPostingListResponse of(
            List<SearchPostingResponse> postings, int page, int size, boolean hasNext
    ) {
        return new SearchPostingListResponse(postings, page, size, hasNext);
    }
}