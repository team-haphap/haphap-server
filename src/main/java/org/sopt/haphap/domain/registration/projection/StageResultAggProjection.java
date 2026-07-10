package org.sopt.haphap.domain.registration.projection;

import org.sopt.haphap.domain.registration.domain.RegistrationResult;

public interface StageResultAggProjection {
    Long getPostingId();
    Long getStageId();
    RegistrationResult getResult();
    long getCnt();
}