package org.sopt.haphap.domain.registration.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 합격 인증(인증샷 검토) 상태. 운영진 승인/반려 흐름 자체는 별도 파트에서 붙일 예정 —
// 여기서는 전형 이동/집계 로직이 참조할 수 있게 상태값과 최소 전이만 정의해둔다.
@Getter
@RequiredArgsConstructor
public enum RegistrationVerificationStatus {
    NOT_REQUIRED("인증 불필요"),   // FAIL/PENDING — 애초에 인증 대상 아님
    PENDING("승인 대기"),         // PASS로 등록되어 운영진 승인을 기다리는 중
    APPROVED("승인됨"),
    REJECTED("반려됨");

    private final String description;
}
