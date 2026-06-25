package org.sopt.haphap.realtime.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.SuccessResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RealtimeSuccessCode implements SuccessResultCode {

    REALTIME_FEED_FOUND(HttpStatus.OK, "실시간 피드를 조회했습니다."),
    REALTIME_SUMMARY_FOUND(HttpStatus.OK, "실시간 요약 정보를 조회했습니다."),
    REALTIME_STREAM_CONNECTED(HttpStatus.OK, "실시간 스트림에 연결되었습니다."),
    STATUS_REPORT_CREATED(HttpStatus.CREATED, "내 상태가 등록되었습니다.");

    private final HttpStatus status;
    private final String message;
}