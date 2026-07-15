package org.sopt.haphap.domain.posting.dto.projection;

import java.time.LocalDate;

public interface PostingStageFlatProjection {
    Long getPostingId();                        // 어느 공고의 전형인지 (그룹핑용)
    Long getStageId();                          // 전형 id (등록수 매칭용)
    String getName();                           // 전형명 → nextStage 이름
    int getOrderIndex();                        // 전형 순서 → nextStage 계산의 핵심
    LocalDate getExpectedAnnouncementDate();    // 발표예정일 → daysUntilNextStage, 정렬키
    LocalDate getAnnouncedDate();
}