package org.sopt.haphap.global.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthSuccessCode implements SuccessResultCode {

    KAKAO_LOGIN_SUCCESS(HttpStatus.OK, "카카오 로그인에 성공했습니다."),
    REISSUE_SUCCESS(HttpStatus.OK, "토큰 재발급에 성공했습니다."),
    LOGOUT_SUCCESS(HttpStatus.NO_CONTENT, "로그아웃에 성공했습니다."),
    AGREEMENT_SUBMIT_SUCCESS(HttpStatus.OK, "약관 동의가 완료되어 가입이 완료되었습니다."),
    ;

    private final HttpStatus status;
    private final String message;
}