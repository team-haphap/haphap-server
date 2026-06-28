package org.sopt.haphap.global.client.dto;

import java.time.LocalDate;

public record OAuthUserInfo(
        String providerId,
        String name,
        String email,
        LocalDate birthDate
) {}