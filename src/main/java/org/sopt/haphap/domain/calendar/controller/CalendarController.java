package org.sopt.haphap.domain.calendar.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.calendar.code.CalendarSuccessCode;
import org.sopt.haphap.domain.calendar.dto.CalendarPostingListResponse;
import org.sopt.haphap.domain.calendar.service.CalendarPostingQueryService;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/calendar")
public class CalendarController implements CalendarApiDocs {

    private final CalendarPostingQueryService calendarPostingQueryService;

    @GetMapping("/postings")
    public ResponseEntity<SuccessResponse<CalendarPostingListResponse>> getPostingsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        CalendarPostingListResponse response = calendarPostingQueryService.getPostingsByDate(date);

        SuccessResponse<CalendarPostingListResponse> body =
                ApiResponse.success(CalendarSuccessCode.CALENDAR_POSTINGS_FETCHED, response);

        return ResponseEntity.status(body.status()).body(body);
    }
}