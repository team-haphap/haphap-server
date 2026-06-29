package org.sopt.haphap.domain.alram.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.ErrorResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AlramErrorCode implements ErrorResultCode {

    POSTING_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 공고입니다.");

    private final HttpStatus status;
    private final String message;
}