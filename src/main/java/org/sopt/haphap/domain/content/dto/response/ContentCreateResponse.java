package org.sopt.haphap.domain.content.dto.response;

import org.sopt.haphap.domain.content.domain.Content;
public record ContentCreateResponse(Long contentId) {
    public static ContentCreateResponse from(Content content) {
        return new ContentCreateResponse(content.getId());
    }
}