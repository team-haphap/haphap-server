package org.sopt.haphap.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record AppleLoginRequest(
        @NotBlank String identityToken,
        String name // 최초 로그인 시에만 iOS가 내려주고, 그 외에는 null
) {}