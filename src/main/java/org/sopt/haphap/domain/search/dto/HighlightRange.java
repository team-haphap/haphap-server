package org.sopt.haphap.domain.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record HighlightRange(
        @Schema(description = "하이라이트 시작 offset (inclusive)") int start,
        @Schema(description = "하이라이트 끝 offset (exclusive)") int end
) {}