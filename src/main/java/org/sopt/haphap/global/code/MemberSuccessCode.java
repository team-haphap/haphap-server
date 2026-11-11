package org.sopt.haphap.global.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberSuccessCode implements SuccessResultCode {

    MEMBER_INFO_FETCHED(HttpStatus.OK, "사용자 정보 조회에 성공했습니다."),
    MEMBER_PROFILE_UPDATED(HttpStatus.OK, "프로필이 수정되었습니다."),
    MEMBER_ACCOUNT_INFO_FETCHED(HttpStatus.OK, "사용자 계정 정보 조회에 성공했습니다."),
    ;

    private final HttpStatus status;
    private final String message;
}