package org.sopt.haphap.domain.registration.event;

import org.sopt.haphap.domain.registration.domain.RegistrationResult;

public record StageResultCountedEvent(
        Long postingId, Long stageId, RegistrationResult result) {}