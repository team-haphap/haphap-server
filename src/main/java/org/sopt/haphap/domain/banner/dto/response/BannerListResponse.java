package org.sopt.haphap.domain.banner.dto.response;

import java.util.List;

public record BannerListResponse(List<BannerResponse> banners) {
    public static BannerListResponse from(List<BannerResponse> banners) {
        return new BannerListResponse(banners);
    }
}