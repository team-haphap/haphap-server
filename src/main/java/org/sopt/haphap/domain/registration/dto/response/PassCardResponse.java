package org.sopt.haphap.domain.registration.dto.response;

import org.sopt.haphap.domain.posting.domain.Category;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.registration.domain.Registration;

public record PassCardResponse(
        Long registrationId,
        String userName,
        String companyName,
        String companyCardLogoImageUrl,
        String title,
        String cardImageUrl,
        boolean confirmed,
        Long stageId,
        String stageName,
        int orderIndex
) {
    public static PassCardResponse from(Registration registration) {
        Posting posting = registration.getPosting();
        Category category = posting.getCategory();
        return new PassCardResponse(
                registration.getId(),
                registration.getUser().getName(),
                posting.getCompany().getName(),
                posting.getCompany().getCardLogoImageUrl(),
                posting.getTitle(),
                category.getCardImageUrl(),
                registration.isPassCardConfirmed(),
                registration.getStage().getId(),
                registration.getStage().getName(),
                registration.getStage().getOrderIndex()
        );
    }
}
