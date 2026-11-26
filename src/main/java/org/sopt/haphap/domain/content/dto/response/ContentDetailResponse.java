package org.sopt.haphap.domain.content.dto.response;

import org.sopt.haphap.domain.content.domain.Content;
import org.sopt.haphap.domain.content.domain.ContentType;

import java.time.LocalDateTime;

public record ContentDetailResponse(
        Long contentId, ContentType type, String title, String body,
        String authorNickname, boolean mine,
        Long postingId, String companyName, String postingTitle,
        LocalDateTime createdAt
) {
    public static ContentDetailResponse of(Content c, Long viewerId) {
        return new ContentDetailResponse(
                c.getId(), c.getType(), c.getTitle(), c.getBody(),
                ContentSummaryResponse.authorNickname(c), c.isWrittenBy(viewerId),
                c.getPosting() == null ? null : c.getPosting().getId(),
                c.getPosting() == null ? null : c.getPosting().getCompany().getName(),
                c.getPosting() == null ? null : c.getPosting().getTitle(),
                c.getCreatedAt());
    }
}