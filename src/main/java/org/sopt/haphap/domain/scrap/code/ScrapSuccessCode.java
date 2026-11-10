package org.sopt.haphap.domain.scrap.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.SuccessResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ScrapSuccessCode implements SuccessResultCode {

    SCRAP_CREATED(HttpStatus.CREATED, "공고를 찜했습니다."),
    ALREADY_SCRAPPED(HttpStatus.OK, "이미 찜한 공고입니다."),
    SCRAP_DELETED(HttpStatus.OK, "공고 찜을 취소했습니다."),
    ALREADY_UNSCRAPPED(HttpStatus.OK, "찜하지 않은 공고입니다."),
    MY_SCRAPS_FETCHED(HttpStatus.OK, "찜한 공고 목록 조회에 성공했습니다.");

    private final HttpStatus status;
    private final String message;
}
