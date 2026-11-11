package org.sopt.haphap.domain.search.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.SuccessResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SearchSuccessCode implements SuccessResultCode {

    POPULAR_POSTINGS_FETCHED(HttpStatus.OK, "인기 공고 목록 조회에 성공했습니다."),
    AUTOCOMPLETE_FETCHED(HttpStatus.OK, "검색 자동완성 조회에 성공했습니다."),
    POSTING_SEARCH_FETCHED(HttpStatus.OK, "검색 결과 공고 목록 조회에 성공했습니다."),
    POPULAR_KEYWORDS_FETCHED(HttpStatus.OK, "인기 검색어 조회에 성공했습니다."),
    SEARCH_EXECUTION_RECORDED(HttpStatus.NO_CONTENT, "검색 실행이 기록되었습니다.");

    private final HttpStatus status;
    private final String message;
}