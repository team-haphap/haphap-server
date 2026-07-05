package org.sopt.haphap.domain.registration.event;

import org.sopt.haphap.domain.registration.domain.RegistrationResult;

public record RegistrationResultChangedEvent(
        Long postingId, Long stageId, RegistrationResult newResult) {}