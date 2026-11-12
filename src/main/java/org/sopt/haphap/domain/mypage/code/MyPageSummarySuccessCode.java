package org.sopt.haphap.domain.mypage.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.SuccessResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MyPageSummarySuccessCode implements SuccessResultCode {

    MY_PAGE_SUMMARY_FETCHED(HttpStatus.OK, "마이페이지 요약 정보 조회에 성공했습니다.");

    private final HttpStatus status;
    private final String message;
}
