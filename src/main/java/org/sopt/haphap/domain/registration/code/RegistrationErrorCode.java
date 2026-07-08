package org.sopt.haphap.domain.registration.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.ErrorResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RegistrationErrorCode implements ErrorResultCode {

    DUPLICATE_REGISTRATION(HttpStatus.CONFLICT, "이미 입력한 전형입니다."),
    REGISTRATION_NOT_FOUND(HttpStatus.NOT_FOUND,"존재하지 않는 등록입니다."),
    PENDING_MUST_NOT_HAVE_CONTACT(HttpStatus.BAD_REQUEST, "대기 상태에서는 연락 정보를 보낼 수 없습니다."),
    CONFIRMED_MUST_HAVE_CONTACT(HttpStatus.BAD_REQUEST, "합격/불합격 결과에는 연락 정보가 필요합니다.");

    private final HttpStatus status;
    private final String message;
}