package org.sopt.haphap.domain.passcard.dto.response;

import java.time.LocalDateTime;
import org.sopt.haphap.domain.registration.domain.Registration;

public record PassCardArchiveItemResponse(
        Long registrationId,
        String companyName,
        String companyLogoImageUrl,
        String title,
        String category,
        String status,    //항상 pass
        LocalDateTime confirmedAt
) {
    public static PassCardArchiveItemResponse from(Registration registration) {
        return new PassCardArchiveItemResponse(
                registration.getId(),
                registration.getPosting().getCompany().getName(),
                registration.getPosting().getCompany().getCardLogoImageUrl(),
                registration.getPosting().getTitle(),
                registration.getPosting().getCategory().getName(),
                "PASS",
                registration.getPassCardConfirmedAt()
        );
    }
}