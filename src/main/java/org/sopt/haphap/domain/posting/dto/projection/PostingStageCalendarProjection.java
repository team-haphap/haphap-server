package org.sopt.haphap.domain.posting.dto.projection;

import java.time.LocalDate;

public interface PostingStageCalendarProjection {
    Long getPostingId();
    Long getStageId();
    String getStageName();
    int getExpectedScore();
    LocalDate getExpectedAnnouncementDate();
    String getTitle();
    String getCompanyImageUrl();
}