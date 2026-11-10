package org.sopt.haphap.domain.scrap.dto.response;

public record ScrapResponse(
        Long postingId,
        boolean isScrapped
) {
    public static ScrapResponse scrapped(Long postingId) {
        return new ScrapResponse(postingId, true);
    }
}
