package org.sopt.haphap.domain.registration.projection;

import org.sopt.haphap.domain.registration.domain.RegistrationResult;
import java.time.LocalDateTime;

public interface RegistrationFeedProjection {
    String getStage();              // 전형명 (예: 서류, 1차 면접)
    String getNickName();// 익명 이름 (anonymousName)
    RegistrationResult getStatus();
    LocalDateTime getFeedCreatedAt(); // 등록/수정 시각 (updatedAt)
}