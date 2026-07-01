package org.sopt.haphap.domain.user.dto;

import jakarta.validation.constraints.NotNull;

public record AgreementSubmitRequest(
        @NotNull(message = "개인정보 수집·이용 동의 여부는 필수입니다.") Boolean privacyPolicyAgreed,
        @NotNull(message = "위치정보 이용약관 동의 여부는 필수입니다.") Boolean locationTermsAgreed,
        @NotNull(message = "만 14세 이상 확인 여부는 필수입니다.") Boolean ageOver14Agreed,
        @NotNull(message = "채널 친구 추가 동의 여부는 필수입니다.") Boolean channelAddAgreed,
        @NotNull(message = "광고/마케팅 수신 동의 여부는 필수입니다.") Boolean marketingAgreed
) {}