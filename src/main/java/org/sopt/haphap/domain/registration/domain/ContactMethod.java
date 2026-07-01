package org.sopt.haphap.domain.registration.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContactMethod {
    EMAIL("이메일"),
    SMS("문자"),
    MY_PAGE("마이페이지"),
    PHONE_CALL("전화"),
    ETC("기타");

    private final String description;
}