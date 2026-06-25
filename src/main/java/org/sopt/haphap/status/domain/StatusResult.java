package org.sopt.haphap.status.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StatusResult {

    PASSED("합격했어요"),
    FAILED("불합격했어요"),
    UNKNOWN("아직 몰라요");

    private final String displayName;
}