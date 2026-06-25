package org.sopt.haphap.realtime.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RealtimeEventType {

    STATUS_REPORT_CREATED("전형 제보가 등록되었습니다."),
    ANNOUNCEMENT_HEATED("공고 등록 속도가 증가했습니다.");

    private final String defaultMessage;
}