package org.sopt.haphap.domain.user.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String signupToken,
        String anonymousName,
        boolean isNewUser
) {
}