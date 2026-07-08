package org.sopt.haphap.domain.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record SearchPostingListResponse(
        List<SearchPostingResponse> postings,
        int page,
        int size,
        @Schema(description = "다음 페이지 존재 여부")
        boolean hasNext
) {
    public static SearchPostingListResponse of(
            List<SearchPostingResponse> postings, int page, int size, boolean hasNext
    ) {
        return new SearchPostingListResponse(postings, page, size, hasNext);
    }
}