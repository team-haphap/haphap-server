package org.sopt.haphap.announcement.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecruitmentStage {

    DOCUMENT("서류", 1),
    CODING_TEST("코딩 테스트", 2),
    FIRST_INTERVIEW("1차 면접", 3),
    SECOND_INTERVIEW("2차 면접", 4),
    EXECUTIVE_INTERVIEW("임원 면접", 5),
    FINAL("최종", 6),
    ETC("기타", 99);

    private final String displayName;
    private final int order;
}