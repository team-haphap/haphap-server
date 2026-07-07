package org.sopt.haphap.domain.calendar.dto;

import org.sopt.haphap.domain.posting.domain.AnnouncementLikelihood;

public record CalendarPostingCardResponse(
        Long postingId,
        String title,
        String stageName,
        AnnouncementLikelihood likelihood,
        long participantCount
) {
}