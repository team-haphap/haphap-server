package org.sopt.haphap.status.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationChannel {

    SMS("문자"),
    EMAIL("메일"),
    MYPAGE("마이페이지"),
    PHONE("전화");

    private final String displayName;
}