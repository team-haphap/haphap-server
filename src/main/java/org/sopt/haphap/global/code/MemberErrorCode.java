package org.sopt.haphap.global.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorResultCode {

    INVALID_NICKNAME_CHARACTER(HttpStatus.BAD_REQUEST, "닉네임은 한글, 영문, 숫자만 사용할 수 있습니다."),
    INVALID_NICKNAME_LENGTH(HttpStatus.BAD_REQUEST, "닉네임은 2~5자로 입력해주세요."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "다른 사용자가 사용중인 닉네임입니다."),
    INVALID_NICKNAME_FORMAT(HttpStatus.BAD_REQUEST, "한글, 영문, 숫자만 사용할 수 있어요."),
    NICKNAME_DUPLICATED(HttpStatus.CONFLICT,"다른 사용자가 사용중인 닉네임이에요."),
    ;

    private final HttpStatus status;
    private final String message;
}
