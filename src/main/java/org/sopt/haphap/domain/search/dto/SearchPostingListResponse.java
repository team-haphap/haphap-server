package org.sopt.haphap.domain.search.dto;

import java.util.List;
import org.sopt.haphap.domain.posting.dto.PopularPostingResponse;

public record SearchPostingListResponse(
        List<PopularPostingResponse> postings,
        int page,
        int size,
        boolean hasNext
) {
    public static SearchPostingListResponse of(
            List<PopularPostingResponse> postings, int page, int size, boolean hasNext
    ) {
        return new SearchPostingListResponse(postings, page, size, hasNext);
    }
}