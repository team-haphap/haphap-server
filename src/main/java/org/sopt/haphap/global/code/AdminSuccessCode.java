package org.sopt.haphap.global.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AdminSuccessCode implements SuccessResultCode {

    ADMIN_LOGIN_SUCCESS(HttpStatus.OK, "관리자 로그인에 성공했습니다."),
    ;

    private final HttpStatus status;
    private final String message;
}