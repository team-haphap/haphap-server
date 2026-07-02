package org.sopt.haphap.domain.registration.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.SuccessResultCode;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum RegistrationSuccessCode implements SuccessResultCode {
    REGISTRATION_CREATED(HttpStatus.CREATED, "상태 등록이 완료되었습니다."),
    REGISTRATION_UPDATED(HttpStatus.OK, "전형 사항이 변경되었습니다."),
    NEW_REGISTRATION(HttpStatus.OK, "해당 전형의 새로운 상태입니다."),
    REGISTRATION_CONFIRM_REQUIRED(HttpStatus.OK, "전형 사항을 변경여부 토글을 띄워주세요.");

    private final HttpStatus status;
    private final String message;

}