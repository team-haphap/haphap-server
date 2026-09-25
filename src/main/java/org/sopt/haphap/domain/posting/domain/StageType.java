package org.sopt.haphap.domain.posting.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 전형 이동 정책(최소 시차표)의 기준이 되는 canonical 전형 단계.
// PostingStage.name은 공고마다 자유 텍스트라 그대로 정책 판정에 쓸 수 없어 별도로 태깅한다.
@Getter
@RequiredArgsConstructor
public enum StageType {
    DOCUMENT("서류"),
    APTITUDE_OR_CODING_TEST("인적성·코딩테스트"),
    FIRST_INTERVIEW("1차면접"),
    SECOND_INTERVIEW("2차면접"),
    EXECUTIVE_INTERVIEW("임원면접"),
    FINAL_PASS("최종합격");

    private final String description;
}
