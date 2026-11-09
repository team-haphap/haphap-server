package org.sopt.haphap.domain.mypage.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.SuccessResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MyPageSuccessCode implements SuccessResultCode {

    MY_REGISTRATIONS_PREVIEW_FETCHED(HttpStatus.OK, "마이페이지 지원 내역 조회에 성공했습니다.");

    private final HttpStatus status;
    private final String message;
}
