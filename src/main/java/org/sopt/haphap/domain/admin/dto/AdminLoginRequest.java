package org.sopt.haphap.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(@NotBlank String loginId, @NotBlank String password) {}