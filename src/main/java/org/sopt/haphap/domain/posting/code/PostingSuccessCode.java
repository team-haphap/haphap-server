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
    POSTING_ALL_LIST_FETCHED(HttpStatus.OK,"공고 전체 조회에 성공했습니다."),
    TODAY_ANNOUNCEMENT_POSTING_FETCHED(HttpStatus.OK,  "오늘 발표 예상 공고 조회에 성공했습니다."),
    POSTING_DETAIL_FETCHED(HttpStatus.OK,"공고 상세 조회에 성공했습니다."),
    POSTING_STAGE_STATUS_FETCHED(HttpStatus.OK,"공고 전형 상태 조회에 성공했습니다."),
    TODAY_STATISTIC_FETCHED(HttpStatus.OK,"오늘 집계 결과 조회에 성공했습니다."),
    POSTING_STAGE_STATISTIC_FETCHED(HttpStatus.OK,"공고 전형별 집계 조회에 성공했습니다.");

    private final HttpStatus status;
    private final String message;
}