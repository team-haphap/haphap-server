package org.sopt.haphap.domain.search.dto;

import java.util.List;

public record PopularSearchPostingListResponse(List<PopularSearchPostingResponse> postings) {

    public static PopularSearchPostingListResponse from(List<PopularSearchPostingResponse> postings) {
        return new PopularSearchPostingListResponse(postings);
    }
}