package org.sopt.haphap.domain.posting.dto.response;

import java.util.List;

public record PostingStageListResponse(Long postingId, List<PostingStageResponse> stages) {

    public static PostingStageListResponse of(Long postingId, List<PostingStageResponse> stages) {
        return new PostingStageListResponse(postingId, stages);
    }
}