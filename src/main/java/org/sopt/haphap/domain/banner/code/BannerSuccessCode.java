package org.sopt.haphap.domain.banner.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.SuccessResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BannerSuccessCode implements SuccessResultCode {

    BANNER_LIST_FETCHED(HttpStatus.OK, "히어로 배너 목록 조회에 성공했습니다.");

    private final HttpStatus status;
    private final String message;
}