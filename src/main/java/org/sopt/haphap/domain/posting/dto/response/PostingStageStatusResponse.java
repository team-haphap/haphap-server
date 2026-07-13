package org.sopt.haphap.domain.posting.dto.response;

import org.sopt.haphap.domain.posting.domain.StageStatus;

public record PostingStageStatusResponse(
        Long stageId,
        String stageName,
        //int orderIndex,
        StageStatus status
) {}