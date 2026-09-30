package org.sopt.haphap.domain.home.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.SuccessResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum HomeSuccessCode implements SuccessResultCode {

    MY_APPLICATIONS_FETCHED(HttpStatus.OK, "내 지원 공고 조회에 성공했습니다."),
    RECENT_VIEWS_FETCHED(HttpStatus.OK, "최근 조회한 공고 조회에 성공했습니다."),
    POPULAR_POSTINGS_FETCHED(HttpStatus.OK, "지금 많이 보는 공고 조회에 성공했습니다.");

    private final HttpStatus status;
    private final String message;
}
