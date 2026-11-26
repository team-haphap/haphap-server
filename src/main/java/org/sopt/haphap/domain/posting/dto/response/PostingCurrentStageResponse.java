package org.sopt.haphap.domain.posting.dto.response;

import java.time.LocalDateTime;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.domain.StageType;

public record PostingCurrentStageResponse(
        Long postingId, Long currentStageId, String currentStageName,
        StageType stageType, LocalDateTime movedAt
) {
    public static PostingCurrentStageResponse from(Posting posting) {
        PostingStage stage = posting.getCurrentStage();
        return new PostingCurrentStageResponse(
                posting.getId(), stage.getId(), stage.getName(), stage.getStageType(), stage.getMovedAt());
    }
}
