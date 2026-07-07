package org.sopt.haphap.domain.calendar.dto;

import org.sopt.haphap.domain.posting.domain.AnnouncementLikelihood;

public record CalendarPostingCardResponse (
        Long postingId,
        String title,
        String stageName,
        AnnouncementLikelihood likelihood,
        long participantCount,
        String companyImageUrl
) {
    public static CalendarPostingCardResponse of(
            Long postingId,
            String title,
            String stageName,
            AnnouncementLikelihood likelihood,
            long participantCount,
            String companyImageUrl
    ) {
        return new CalendarPostingCardResponse(
                postingId,
                title,
                stageName,
                likelihood,
                participantCount,
                companyImageUrl
        );
    }
}