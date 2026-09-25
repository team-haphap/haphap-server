package org.sopt.haphap.global.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties (
    String secret,
    Duration accessTokenExpiry,
    Duration refreshTokenExpiry
){}
