package org.sopt.haphap.domain.posting.dto.response;

import java.util.List;

public record TodayAnnouncementPostingListResponse(
        List<TodayAnnouncementPostingResponse> postings
) {
    public static TodayAnnouncementPostingListResponse from(
            List<TodayAnnouncementPostingResponse> postings) {
        return new TodayAnnouncementPostingListResponse(postings);
    }
}