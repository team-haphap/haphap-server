package org.sopt.haphap.domain.search.dto;

public record SearchPostingResponse(
        Long postingId,
        String companyName,
        String title,
        String categoryName,
        Integer dDay,
        String status
) {
}