package org.sopt.haphap.domain.search.dto;

public record PopularSearchPostingResponse(
        Long postingId,
        String companyName,
        String title,
        String categoryName,
        Integer dDay
) {
}