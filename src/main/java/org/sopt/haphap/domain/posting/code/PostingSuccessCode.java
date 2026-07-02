package org.sopt.haphap.domain.posting.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.SuccessResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PostingSuccessCode implements SuccessResultCode {

    POSTING_LIST_FETCHED(HttpStatus.OK, "공고명 목록 조회에 성공했습니다."),
    POSTING_STAGE_LIST_FETCHED(HttpStatus.OK, "공고별 전형 단계 조회에 성공했습니다."),
    POPULAR_POSTINGS_FETCHED(HttpStatus.OK, "최근 결과가 올라온 공고 전체 조회에 성공했습니다."),
    POSTING_ALL_LIST_FETCHED(HttpStatus.OK,"공고 전체 조회에 성공했습니다.");

    private final HttpStatus status;
    private final String message;
}