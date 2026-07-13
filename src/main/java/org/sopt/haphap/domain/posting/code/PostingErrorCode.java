package org.sopt.haphap.domain.posting.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.ErrorResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PostingErrorCode implements ErrorResultCode {

    POSTING_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 공고입니다."),
    STAGE_NOT_FOUND(HttpStatus.NOT_FOUND,"존재하지 않는 전형입니다."),
    STAGE_NOT_IN_POSTING(HttpStatus.BAD_REQUEST, "해당 공고의 전형 단계가 아닙니다."),

    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."),
    COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회사입니다."),
    DUPLICATE_CATEGORY_NAME(HttpStatus.CONFLICT, "이미 존재하는 카테고리명입니다."),
    DUPLICATE_COMPANY_NAME(HttpStatus.CONFLICT, "이미 존재하는 회사명입니다."),
    DUPLICATE_STAGE_ORDER(HttpStatus.CONFLICT, "이미 존재하는 전형 순서입니다.");

    private final HttpStatus status;
    private final String message;
}