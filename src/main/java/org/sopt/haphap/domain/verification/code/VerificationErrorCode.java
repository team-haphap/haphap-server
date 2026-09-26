package org.sopt.haphap.domain.verification.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.ErrorResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum VerificationErrorCode implements ErrorResultCode {
    IMAGE_COUNT_INVALID(HttpStatus.BAD_REQUEST, "인증 이미지는 1장 이상 3장 이하로 등록해주세요.");
    // TODO. #209에서 추가 예정: IMAGE_REQUIRED, IMAGE_NOT_ALLOWED, IMAGE_NOT_OWNED(403), ALREADY_REVIEWED(409)

    private final HttpStatus status;
    private final String message;
}