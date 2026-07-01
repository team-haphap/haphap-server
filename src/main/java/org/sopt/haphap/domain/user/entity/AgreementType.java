package org.sopt.haphap.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AgreementType {
    PRIVACY_POLICY(true),   // 개인정보 수집·이용 (필수)
    LOCATION_TERMS(true),   // 위치정보 이용약관 (필수)
    AGE_OVER_14(true),      // 만 14세 이상 확인 (필수)
    CHANNEL_ADD(false),     // 채널 친구 추가 (선택)
    MARKETING(false);       // 광고/마케팅 수신 (선택)

    private final boolean required;
}

//이거는 기능명세서에서 동의내용을 카카오 로그인 그다음으로 받길래 만들었습니다.