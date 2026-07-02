package org.sopt.haphap.domain.posting.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.SuccessResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PostingSuccessCode implements SuccessResultCode {

    POSTING_LIST_FETCHED(HttpStatus.OK, "공고명 목록 조회에 성공했습니다."),
    POSTING_STAGE_LIST_FETCHED(HttpStatus.OK, "공고별 전형 단계 조회에 성공했습니다.");

    private final HttpStatus status;
    private final String message;
}