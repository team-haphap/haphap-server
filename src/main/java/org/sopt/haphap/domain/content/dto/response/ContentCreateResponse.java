package org.sopt.haphap.domain.content.dto.response;

public record ContentCreateResponse(Long contentId) {
    public static ContentCreateResponse from(Content content) {
        return new ContentCreateResponse(content.getId());
    }
}