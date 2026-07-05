package org.sopt.haphap.domain.posting.dto.response;

import java.util.List;

public record PopularPostingListResponse(List<PopularPostingResponse> postings) {

    public static PopularPostingListResponse from(List<PopularPostingResponse> postings) {
        return new PopularPostingListResponse(postings);
    }
}