package org.sopt.haphap.domain.passcard.dto.response;

import java.util.List;
import org.sopt.haphap.domain.registration.domain.Registration;
import org.sopt.haphap.domain.registration.dto.response.PassCardResponse;

public record PassCardDetailResponse(
        Long registrationId,
        String userName,
        String companyName,
        String companyCardLogoImageUrl,
        String title,
        String cardImageUrl,
        boolean confirmed,
        Long stageId,
        String stageName,
        int orderIndex,
        List<PassCardStageOptionResponse> availableStages
) {
    public static PassCardDetailResponse from(
            Registration registration,
            List<PassCardStageOptionResponse> availableStages
    ) {
        PassCardResponse card = PassCardResponse.from(registration);
        return new PassCardDetailResponse(
                card.registrationId(),
                card.userName(),
                card.companyName(),
                card.companyCardLogoImageUrl(),
                card.title(),
                card.cardImageUrl(),
                card.confirmed(),
                card.stageId(),
                card.stageName(),
                card.orderIndex(),
                availableStages
        );
    }
}
