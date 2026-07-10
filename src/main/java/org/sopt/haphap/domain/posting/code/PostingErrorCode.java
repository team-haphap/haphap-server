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
    STAGE_NOT_IN_POSTING(HttpStatus.BAD_REQUEST, "해당 공고의 전형 단계가 아닙니다.");

    private final HttpStatus status;
    private final String message;
}