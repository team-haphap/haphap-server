package org.sopt.haphap.domain.scrap.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.SuccessResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ScrapSuccessCode implements SuccessResultCode {

    SCRAP_CREATED(HttpStatus.CREATED, "공고를 찜했습니다."),
    ALREADY_SCRAPPED(HttpStatus.OK, "이미 찜한 공고입니다.");

    private final HttpStatus status;
    private final String message;
}
