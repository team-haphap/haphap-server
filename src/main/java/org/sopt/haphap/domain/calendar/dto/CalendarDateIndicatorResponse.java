package org.sopt.haphap.domain.calendar.dto;

import org.sopt.haphap.domain.posting.domain.AnnouncementLikelihood;

import java.time.LocalDate;

public record CalendarDateIndicatorResponse(LocalDate date, AnnouncementLikelihood likelihood) {
    public static CalendarDateIndicatorResponse of(LocalDate date, AnnouncementLikelihood likelihood) {
        return new CalendarDateIndicatorResponse(date, likelihood);
    }
}