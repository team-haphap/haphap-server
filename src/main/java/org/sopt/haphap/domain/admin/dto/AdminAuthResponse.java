package org.sopt.haphap.domain.admin.dto;

public record AdminAuthResponse(String accessToken, String refreshToken, String name) {}