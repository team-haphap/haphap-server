package org.sopt.haphap.domain.posting.dto.response;

import org.sopt.haphap.domain.posting.domain.StageResultCount;

public record PostingStageStatisticResponse(
        Long stageId,
        Long passCount,
        Long failCount,
        Long pendingCount
) {
    public static PostingStageStatisticResponse of(Long stageId, StageResultCount count) {
        return new PostingStageStatisticResponse(
                stageId, count.getPassCount(), count.getFailCount(), count.getPendingCount());
    }

    public static PostingStageStatisticResponse empty(Long stageId) {
        return new PostingStageStatisticResponse(stageId, 0L, 0L, 0L);
    }
}