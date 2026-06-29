package org.sopt.haphap.domain.alram.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.SuccessResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AlramSuccessCode implements SuccessResultCode {
    DEVICE_TOKEN_REGISTERED(HttpStatus.OK, "디바이스 토큰이 등록되었습니다.");

    private final HttpStatus status;
    private final String message;
}
