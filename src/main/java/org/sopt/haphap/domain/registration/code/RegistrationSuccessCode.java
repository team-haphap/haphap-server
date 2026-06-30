package org.sopt.haphap.domain.registration.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.SuccessResultCode;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum RegistrationSuccessCode implements SuccessResultCode {
    REGISTRATION_CREATED(HttpStatus.CREATED, "상태 등록이 완료되었습니다.");

    private final HttpStatus status;
    private final String message;

}