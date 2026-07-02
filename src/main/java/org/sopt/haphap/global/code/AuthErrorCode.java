package org.sopt.haphap.global.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorResultCode {

    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "저장된 리프레시 토큰과 일치하지 않습니다."),
    KAKAO_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 카카오 액세스 토큰입니다."),
    KAKAO_ACCOUNT_NOT_FOUND(HttpStatus.BAD_REQUEST, "카카오 계정 정보를 가져올 수 없습니다."),
    KAKAO_SERVER_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "카카오 서버 응답이 원활하지 않습니다. 잠시 후 다시 시도해주세요."),
    ;

    private final HttpStatus status;
    private final String message;
}