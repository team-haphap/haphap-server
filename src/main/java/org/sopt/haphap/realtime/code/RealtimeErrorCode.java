package org.sopt.haphap.realtime.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.ErrorResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RealtimeErrorCode implements ErrorResultCode {

    ANNOUNCEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 공고입니다."),
    INVALID_REALTIME_CURSOR(HttpStatus.BAD_REQUEST, "유효하지 않은 실시간 커서입니다."),
    REALTIME_STREAM_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "실시간 이벤트 전송에 실패했습니다.");

    private final HttpStatus status;
    private final String message;
}