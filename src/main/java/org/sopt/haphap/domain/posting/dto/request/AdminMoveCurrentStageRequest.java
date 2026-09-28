package org.sopt.haphap.domain.posting.dto.request;

import jakarta.validation.constraints.NotNull;

public record AdminMoveCurrentStageRequest(
        @NotNull(message = "이동할 전형 ID는 필수입니다.") Long stageId
) {}
