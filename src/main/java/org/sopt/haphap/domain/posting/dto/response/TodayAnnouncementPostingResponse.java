package org.sopt.haphap.domain.posting.dto.response;

import org.sopt.haphap.domain.posting.dto.projection.TodayAnnouncementProjection;

public record TodayAnnouncementPostingResponse(
        Long id,
        String title,
        String companyName,
        String category,
        String stageName,
        String imageUrl
) {
    public static TodayAnnouncementPostingResponse from(TodayAnnouncementProjection p) {
        return new TodayAnnouncementPostingResponse(
                p.getPostingId(), p.getTitle(), p.getCompanyName(),
                p.getCategoryName(), p.getStageName(), p.getImageUrl());
    }
}