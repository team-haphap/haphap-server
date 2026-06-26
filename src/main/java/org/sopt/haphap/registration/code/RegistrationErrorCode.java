package org.sopt.haphap.registration.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.ErrorResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RegistrationErrorCode implements ErrorResultCode {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    POSTING_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 공고입니다.");

    private final HttpStatus status;
    private final String message;
}