package org.sopt.haphap.domain.posting.dto.projection;

public interface TodayAnnouncementProjection {
    Long getStageId();
    String getStageName();
    int getExpectedScore();
    Long getPostingId();
    String getTitle();
    String getCompanyName();
    String getCategoryName();
    String getImageUrl();
}