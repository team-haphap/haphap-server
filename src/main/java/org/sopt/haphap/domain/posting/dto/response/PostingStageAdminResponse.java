package org.sopt.haphap.domain.posting.dto.response;

import org.sopt.haphap.domain.posting.domain.PostingStage;

import java.time.LocalDate;

public record PostingStageAdminResponse(
        Long stageId, Long postingId, String name, int orderIndex,
        LocalDate expectedAnnouncementDate, int expectedScore
) {
    public static PostingStageAdminResponse from(PostingStage stage) {
        return new PostingStageAdminResponse(
                stage.getId(), stage.getPosting().getId(), stage.getName(), stage.getOrderIndex(),
                stage.getExpectedAnnouncementDate(), stage.getExpectedScore());
    }
}
