package org.sopt.haphap.domain.search.dto;

import java.util.List;

public record PopularKeywordListResponse(List<PopularKeywordResponse> keywords) {

    public static PopularKeywordListResponse from(List<PopularKeywordResponse> keywords) {
        return new PopularKeywordListResponse(keywords);
    }
}