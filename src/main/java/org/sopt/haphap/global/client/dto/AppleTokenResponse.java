package org.sopt.haphap.global.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AppleTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("id_token") String idToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") Integer expiresIn
) {}