package org.sopt.haphap.domain.posting.dto.response;

import java.util.List;

public record PostingStageStatusListResponse(
        List<PostingStageStatusResponse> stages,
        Long defaultSelectedStageId    // currentStage id (없으면 null)
) {
    public static PostingStageStatusListResponse of(
            List<PostingStageStatusResponse> stages, Long defaultSelectedStageId) {
        return new PostingStageStatusListResponse(stages, defaultSelectedStageId);
    }
}