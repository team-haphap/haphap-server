package org.sopt.haphap.domain.content.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.SuccessResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ContentSuccessCode implements SuccessResultCode {
    CONTENT_CREATED(HttpStatus.CREATED, "콘텐츠가 등록되었습니다."),
    CONTENT_LIST_FETCHED(HttpStatus.OK, "콘텐츠 목록 조회에 성공했습니다."),
    CONTENT_FETCHED(HttpStatus.OK, "콘텐츠 조회에 성공했습니다."),
    MY_CONTENT_LIST_FETCHED(HttpStatus.OK, "내가 작성한 콘텐츠 조회에 성공했습니다.");

    private final HttpStatus status;
    private final String message;
}