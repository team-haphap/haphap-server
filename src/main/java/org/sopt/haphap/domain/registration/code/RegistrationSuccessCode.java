package org.sopt.haphap.domain.registration.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.SuccessResultCode;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum RegistrationSuccessCode implements SuccessResultCode {
    REGISTRATION_CREATED(HttpStatus.CREATED, "상태 등록이 완료되었습니다."),
    REGISTRATION_UPDATED(HttpStatus.OK, "전형 사항이 변경되었습니다."),
    NEW_REGISTRATION(HttpStatus.OK, "해당 전형의 새로운 상태입니다."),
    REGISTRATION_CONFIRM_REQUIRED(HttpStatus.OK, "전형 사항을 변경여부 토글을 띄워주세요."),
    REVIEW_LIST_FETCHED(HttpStatus.OK, "검토 대상 목록 조회에 성공했습니다."),
    VERIFICATION_SUMMARY_FETCHED(HttpStatus.OK, "합격 인증 현황 조회에 성공했습니다."),
    VERIFICATION_RESUBMITTED(HttpStatus.OK, "합격 인증 이미지가 재등록되었습니다."),
    REVIEW_RESOLVED(HttpStatus.OK, "검토 처리가 완료되었습니다.");

    private final HttpStatus status;
    private final String message;

}