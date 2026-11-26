package org.sopt.haphap.domain.verification.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.ErrorResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum VerificationErrorCode implements ErrorResultCode {
    IMAGE_COUNT_INVALID(HttpStatus.BAD_REQUEST, "인증 이미지는 1장 이상 3장 이하로 등록해주세요."),
    IMAGE_FORMAT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "JPG, JPEG, PNG 이미지만 업로드할 수 있습니다."),
    IMAGE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "이미지는 장당 10MB 이하로 업로드해주세요."),
    IMAGE_REQUIRED(HttpStatus.BAD_REQUEST, "합격 등록에는 인증 이미지가 필요합니다."),
    IMAGE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "합격이 아닌 경우 인증 이미지를 첨부할 수 없습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 인증 이미지입니다."),
    IMAGE_NOT_OWNED(HttpStatus.FORBIDDEN, "본인이 업로드한 이미지가 아닙니다."),
    IMAGE_ALREADY_ATTACHED(HttpStatus.CONFLICT, "이미 다른 등록에 연결된 이미지입니다.");

    private final HttpStatus status;
    private final String message;
}