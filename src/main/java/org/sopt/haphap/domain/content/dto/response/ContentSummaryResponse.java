package org.sopt.haphap.domain.content.dto.response;

//목록, 내 콘텐츠 공용 카드

import org.sopt.haphap.domain.content.domain.Content;
import org.sopt.haphap.domain.content.domain.ContentType;

import java.time.LocalDateTime;

public record ContentSummaryResponse(
        Long contentId,
        ContentType type,
        String title,
        String bodyPreview,       // 본문 앞 100자
        String authorNickname,    // 익명 닉네임, 탈퇴자는 "탈퇴한 사용자"
        Long postingId,           // 연결 공고 없으면 null
        String companyName,
        String postingTitle,
        LocalDateTime createdAt
) {
    private static final int PREVIEW_LENGTH = 100;
    private static final String WITHDRAWN_AUTHOR = "탈퇴한 사용자";

    public static ContentSummaryResponse from(Content c) {
        return new ContentSummaryResponse(
                c.getId(), c.getType(), c.getTitle(),
                preview(c.getBody()), authorNickname(c),
                c.getPosting() == null ? null : c.getPosting().getId(),
                c.getPosting() == null ? null : c.getPosting().getCompany().getName(),
                c.getPosting() == null ? null : c.getPosting().getTitle(),
                c.getCreatedAt());
    }

    private static String preview(String body) {
        if (body.codePointCount(0, body.length()) <= PREVIEW_LENGTH) {
            return body;
        }
        return body.substring(0, body.offsetByCodePoints(0, PREVIEW_LENGTH));
    }

    static String authorNickname(Content c) {
        return (c.getUser() == null || !c.getUser().isActive())
                ? WITHDRAWN_AUTHOR
                : c.getUser().getAnonymousName();
    }
}