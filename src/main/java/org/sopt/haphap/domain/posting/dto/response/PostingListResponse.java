package org.sopt.haphap.domain.posting.dto.response;

import java.util.List;

public record PostingListResponse(List<PostingSummaryResponse> postings) {

    public static PostingListResponse from(List<PostingSummaryResponse> postings) {
        return new PostingListResponse(postings);
    }
}
