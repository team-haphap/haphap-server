package org.sopt.haphap.domain.content.dto.response;

import java.util.List;

public record ContentListResponse(List<ContentSummaryResponse> contents, int page, int size, boolean hasNext) {
    public static ContentListResponse of(Slice<Content> slice, int page, int size) {
        return new ContentListResponse(
                slice.getContent().stream().map(ContentSummaryResponse::from).toList(),
                page, size, slice.hasNext());
    }
}