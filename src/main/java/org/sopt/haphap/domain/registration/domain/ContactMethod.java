package org.sopt.haphap.domain.registration.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContactMethod {
    EMAIL("이메일"),
    SMS("문자"),
    PHONE_CALL("전화"),
    KAKAO("카카오톡"),
    ETC("기타");

    private final String description;
}