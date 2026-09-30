package org.sopt.haphap.domain.verification.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.SuccessResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum VerificationSuccessCode implements SuccessResultCode {
    VERIFICATION_IMAGE_UPLOADED(HttpStatus.CREATED, "인증 이미지가 업로드되었습니다.");

    private final HttpStatus status;
    private final String message;
}