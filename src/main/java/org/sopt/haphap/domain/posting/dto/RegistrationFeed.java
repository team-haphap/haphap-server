package org.sopt.haphap.domain.posting.dto;

import org.sopt.haphap.domain.registration.domain.RegistrationResult;
import java.time.LocalDateTime;

public record RegistrationFeed(
        Long registrationId,
        String stage,
        String nickName,
        RegistrationResult status,
        LocalDateTime feedCreatedAt
) {}
