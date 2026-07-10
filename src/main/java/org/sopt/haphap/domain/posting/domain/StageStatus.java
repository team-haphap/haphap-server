package org.sopt.haphap.domain.posting.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StageStatus {
    COMPLETED("완료"),
    IN_PROGRESS("진행중"),
    UPCOMING("대기중");

    private final String description;
}
