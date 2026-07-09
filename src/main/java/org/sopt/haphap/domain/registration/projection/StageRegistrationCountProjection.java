package org.sopt.haphap.domain.registration.projection;

public interface StageRegistrationCountProjection {
    Long getPostingId();
    Long getStageId();
    long getCnt();
}