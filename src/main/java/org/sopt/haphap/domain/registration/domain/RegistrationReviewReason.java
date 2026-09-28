package org.sopt.haphap.domain.registration.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/*
 * 운영진 검토 기준 중 자동 감지가 필요한 것만 남김.
 * 합격(PASS)은 인증샷을 운영진이 항상 수동으로 승인하기로 확정되어, 후속 전형 관련 판단을
 * 그 승인 과정에서 운영진이 직접 하므로 별도 자동 감지가 불필요함. 불합격(FAIL)은 인증 절차가
 * 없어 운영진이 볼 계기가 원천적으로 없는 두 가지만 자동 감지 대상으로 남긴다.
 */
@Getter
@RequiredArgsConstructor
public enum RegistrationReviewReason {
    REPEATED_FAILURE("합격 인증 없이 하루 내 불합격 2건 이상"),
    SUBSEQUENT_STAGE_FAIL("후속 전형에 불합격 결과가 등록됨");

    private final String description;
}
