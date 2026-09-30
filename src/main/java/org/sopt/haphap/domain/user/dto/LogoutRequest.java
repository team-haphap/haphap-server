package org.sopt.haphap.domain.user.dto;

public record LogoutRequest(
        String deviceId   // 없으면 토큰만 폐기, 있으면 그 기기의 push token도 비활성화
) {}