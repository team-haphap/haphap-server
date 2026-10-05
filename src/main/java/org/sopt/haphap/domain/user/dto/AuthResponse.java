package org.sopt.haphap.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String name,
        String anonymousName,
        String profileImageUrl,
        @Schema(description = "이번 로그인으로 새로 가입된 유저인지 (재발급 응답에서는 항상 false)", example = "true")
        boolean isNewUser
) {
}