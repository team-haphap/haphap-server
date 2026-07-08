package org.sopt.haphap.domain.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SearchPostingResponse(
        Long postingId,
        String companyName,
        String title,
        String categoryName,
        @Schema(description = "다음 전형 발표까지 남은 일수. 발표예정일이 지났는데 다음 상태가 아직 미등록이면 음수 가능", example = "3")
        Integer dDay
) {
}