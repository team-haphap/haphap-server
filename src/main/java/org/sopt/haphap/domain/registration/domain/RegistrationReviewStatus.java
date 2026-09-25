package org.sopt.haphap.domain.registration.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RegistrationReviewStatus {
    PENDING("검토 대기"),
    ACCEPTED("유효 데이터로 인정"),
    REJECTED("무효 처리");

    private final String description;
}
