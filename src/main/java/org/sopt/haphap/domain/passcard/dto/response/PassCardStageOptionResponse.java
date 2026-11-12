package org.sopt.haphap.domain.passcard.dto.response;

import org.sopt.haphap.domain.registration.domain.Registration;

public record PassCardStageOptionResponse(
        Long registrationId,
        Long stageId,
        String stageName,
        int orderIndex,
        boolean confirmed
) {
    public static PassCardStageOptionResponse from(Registration registration) {
        return new PassCardStageOptionResponse(
                registration.getId(),
                registration.getStage().getId(),
                registration.getStage().getName(),
                registration.getStage().getOrderIndex(),
                registration.isPassCardConfirmed()
        );
    }
}
