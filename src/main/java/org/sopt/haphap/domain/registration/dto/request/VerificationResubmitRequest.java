package org.sopt.haphap.domain.registration.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record VerificationResubmitRequest(
        @Schema(description = "재업로드한 인증 이미지 id 목록 (1~3개). 이미지는 POST /registrations/verification-images 로 먼저 업로드")
        @NotNull(message = "인증 이미지는 필수입니다.")
        List<Long> verificationImageIds
) {
}