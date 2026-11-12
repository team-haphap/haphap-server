package org.sopt.haphap.domain.content.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record ContentCreateRequest(

        @NotNull(message = "콘텐츠 타입은 필수입니다.")
        @Schema(description = "REVIEW(후기) / ESSAY(자소서)")
        ContentType type,

        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자 이하로 입력해주세요.")
        String title,

        @NotBlank(message = "본문은 필수입니다.")
        @Size(max = 2000, message = "본문은 2000자 이하로 입력해주세요.")
        String body,

        @Schema(description = "후기 대상 공고 ID (선택)")
        Long postingId
) {
}
