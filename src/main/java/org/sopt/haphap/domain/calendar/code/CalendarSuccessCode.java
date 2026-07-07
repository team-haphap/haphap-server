package org.sopt.haphap.domain.calendar.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.SuccessResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CalendarSuccessCode implements SuccessResultCode {

    CALENDAR_POSTINGS_FETCHED(HttpStatus.OK, "날짜별 공고 카드 조회에 성공했습니다."),
    CALENDAR_INDICATOR_FETCHED(HttpStatus.OK, "월별 캘린더 인디케이터 조회에 성공했습니다.");

    private final HttpStatus status;
    private final String message;
}