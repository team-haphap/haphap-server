package org.sopt.haphap.domain.user.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String anonymousName,  //익명의 xxx
        Boolean isNewUser //와프에서 신규가입 vs 기존 로그인 구분
) {
}