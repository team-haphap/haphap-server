package org.sopt.haphap.domain.posting.dto.response;

import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.domain.StageType;

import java.time.LocalDate;

public record PostingStageAdminResponse(
        Long stageId, Long postingId, String name, int orderIndex,
        LocalDate expectedAnnouncementDate, int expectedScore, StageType stageType
) {
    public static PostingStageAdminResponse from(PostingStage stage) {
        return new PostingStageAdminResponse(
                stage.getId(), stage.getPosting().getId(), stage.getName(), stage.getOrderIndex(),
                stage.getExpectedAnnouncementDate(), stage.getExpectedScore(), stage.getStageType());
    }
}
