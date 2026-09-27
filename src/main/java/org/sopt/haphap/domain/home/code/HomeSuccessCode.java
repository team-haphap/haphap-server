package org.sopt.haphap.domain.home.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.SuccessResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum HomeSuccessCode implements SuccessResultCode {

    MY_APPLICATIONS_FETCHED(HttpStatus.OK, "내 지원 공고 조회에 성공했습니다.");

    private final HttpStatus status;
    private final String message;
}
