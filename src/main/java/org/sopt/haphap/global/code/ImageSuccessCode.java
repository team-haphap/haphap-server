package org.sopt.haphap.global.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ImageSuccessCode implements SuccessResultCode {

    IMAGE_UPLOADED(HttpStatus.CREATED, "이미지가 업로드되었습니다.");

    private final HttpStatus status;
    private final String message;
}