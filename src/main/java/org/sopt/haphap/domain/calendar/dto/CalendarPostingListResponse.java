package org.sopt.haphap.domain.calendar.dto;

import java.time.LocalDate;
import java.util.List;

public record CalendarPostingListResponse(LocalDate date, List<CalendarPostingCardResponse> postings) {

    public static CalendarPostingListResponse of(LocalDate date, List<CalendarPostingCardResponse> postings) {
        return new CalendarPostingListResponse(date, postings);
    }
}