package org.sopt.haphap.domain.posting.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PopularPostingResponse(
        Long id,
        String title,
        String companyName,
        String category,
        String content,
        @Schema(description = "다음 전형명. 모든 전형이 진행 임계치 이상 도달(마감)이면 null", nullable = true)
        String nextStage,
        @Schema(description = "다음 전형 발표까지 남은 일수. 발표예정일이 지나면 음수 가능", nullable = true)
        Integer daysUntilNextStage,
        String imageUrl
) {
}