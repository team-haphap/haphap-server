package org.sopt.haphap.domain.registration.dto.response;

import org.sopt.haphap.domain.posting.domain.Category;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.registration.domain.Registration;

public record PassCardResponse(
        String userName,
        String companyName,
        String companyLogoImageUrl,
        String title,
        String stageName,
        String categoryName,
        String cardImageUrl
) {
    public static PassCardResponse from(Registration registration) {
        Posting posting = registration.getPosting();
        Category category = posting.getCategory();
        return new PassCardResponse(
                registration.getUser().getName(),
                posting.getCompany().getName(),
                posting.getCompany().getLogoImageUrl(),
                posting.getTitle(),
                registration.getStage().getName(),
                category.getName(),
                category.getCardImageUrl()
        );
    }
}