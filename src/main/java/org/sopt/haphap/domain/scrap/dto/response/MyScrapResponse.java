package org.sopt.haphap.domain.scrap.dto.response;

import org.sopt.haphap.domain.scrap.projection.MyScrapProjection;

public record MyScrapResponse(
        Long postingId,
        String companyName,
        String category,
        String title,
        String logoImageUrl
) {
    public static MyScrapResponse from(MyScrapProjection p) {
        return new MyScrapResponse(p.getPostingId(), p.getCompanyName(), p.getCategoryName(), p.getTitle(),
                p.getLogoImageUrl());
    }
}
