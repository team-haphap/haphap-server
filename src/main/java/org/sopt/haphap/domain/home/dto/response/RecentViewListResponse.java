package org.sopt.haphap.domain.home.dto.response;

import java.util.List;

public record RecentViewListResponse(List<RecentViewResponse> views) {

    public static RecentViewListResponse from(List<RecentViewResponse> views) {
        return new RecentViewListResponse(views);
    }
}
