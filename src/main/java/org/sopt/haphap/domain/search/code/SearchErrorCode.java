package org.sopt.haphap.domain.search.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.ErrorResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SearchErrorCode implements ErrorResultCode {

    KEYWORD_REQUIRED(HttpStatus.BAD_REQUEST, "검색어 입력은 필수입니다."),
    RELATED_KEYWORD_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 관련 검색어입니다."),
    SEARCH_ENGINE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "검색 서비스가 일시적으로 원활하지 않습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus status;
    private final String message;
}