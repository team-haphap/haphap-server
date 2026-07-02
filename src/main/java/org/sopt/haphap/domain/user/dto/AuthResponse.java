package org.sopt.haphap.domain.user.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String name,
        String anonymousName
) {
}