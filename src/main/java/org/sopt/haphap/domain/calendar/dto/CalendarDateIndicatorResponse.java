package org.sopt.haphap.domain.calendar.dto;

import org.sopt.haphap.domain.posting.domain.AnnouncementLikelihood;

import java.time.LocalDate;

public record CalendarDateIndicatorResponse(LocalDate date, AnnouncementLikelihood probability) {
    public static CalendarDateIndicatorResponse of(LocalDate date, AnnouncementLikelihood probability) {
        return new CalendarDateIndicatorResponse(date, probability);
    }
}