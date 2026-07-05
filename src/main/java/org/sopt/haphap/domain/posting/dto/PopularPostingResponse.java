package org.sopt.haphap.domain.posting.dto;

public record PopularPostingResponse(
        Long id,
        String title,
        String companyName,
        String category,
        String content,
        String nextStage,          // 없으면 null
        Integer daysUntilNextStage, // 없으면 null (Integer로 nullable 허용)
        String imageUrl
) {
}