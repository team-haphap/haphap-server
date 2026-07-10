package org.sopt.haphap.domain.calendar.dto;

import java.util.List;

public record CalendarIndicatorListResponse(List<CalendarDateIndicatorResponse> dates) {
    public static CalendarIndicatorListResponse of(List<CalendarDateIndicatorResponse> dates) {
        return new CalendarIndicatorListResponse(dates);
    }
}