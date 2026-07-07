package org.sopt.haphap.domain.calendar.service;

import org.sopt.haphap.domain.posting.dto.projection.PostingStageCalendarProjection;

public final class CalendarStageMerger {

    private CalendarStageMerger() {}

    // 점수가 같으면 stageId가 작은(먼저 생성된) 전형을 대표로 고정
    public static PostingStageCalendarProjection pickHigherScore(PostingStageCalendarProjection a, PostingStageCalendarProjection b) {
        if (a.getExpectedScore() != b.getExpectedScore()) {
            return a.getExpectedScore() > b.getExpectedScore() ? a : b;
        }
        return a.getStageId() <= b.getStageId() ? a : b;
    }
}