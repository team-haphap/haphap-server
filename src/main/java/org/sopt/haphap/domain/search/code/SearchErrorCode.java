package org.sopt.haphap.domain.search.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.ErrorResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SearchErrorCode implements ErrorResultCode {

    KEYWORD_REQUIRED(HttpStatus.BAD_REQUEST, "검색어 입력은 필수입니다."),
    CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 카테고리입니다."),
    RELATED_KEYWORD_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 관련 검색어입니다.");

    private final HttpStatus status;
    private final String message;
}