package org.sopt.haphap.domain.calendar.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.ErrorResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CalendarErrorCode implements ErrorResultCode {

    UNSUPPORTED_DATE_RANGE(HttpStatus.BAD_REQUEST, "지원하지 않는 날짜 범위입니다. (2000-01 ~ 2030-12)"),
    ;

    private final HttpStatus status;
    private final String message;
}