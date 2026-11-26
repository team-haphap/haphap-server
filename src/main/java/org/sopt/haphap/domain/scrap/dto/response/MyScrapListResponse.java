package org.sopt.haphap.domain.scrap.dto.response;

import java.util.List;

public record MyScrapListResponse(
        int totalCount,
        List<MyScrapResponse> scraps
) {
    public static MyScrapListResponse from(List<MyScrapResponse> scraps) {
        return new MyScrapListResponse(scraps.size(), scraps);
    }
}
