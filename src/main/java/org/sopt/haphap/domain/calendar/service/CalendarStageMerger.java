package org.sopt.haphap.domain.calendar.service;

import org.sopt.haphap.domain.posting.dto.projection.PostingStageCalendarProjection;

public final class CalendarStageMerger {

    private CalendarStageMerger() {}

    /**
     * 서로 다른 공고 간 동점 처리 전용 (CalendarIndicatorQueryService에서 날짜별 색상 등급을 정할 때 사용).
     * 같은 공고 내 대표 전형 선택은 CalendarRepresentativeStageResolver를 사용할 것. -> 얘도 예외상황에만 쓰도록 해놨어요
     */

    public static PostingStageCalendarProjection pickHigherScore(PostingStageCalendarProjection a, PostingStageCalendarProjection b) {
        if (a.getExpectedScore() != b.getExpectedScore()) {
            return a.getExpectedScore() > b.getExpectedScore() ? a : b;
        }
        return a.getStageId() <= b.getStageId() ? a : b;
    }
}