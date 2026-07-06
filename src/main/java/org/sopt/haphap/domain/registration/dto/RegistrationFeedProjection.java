package org.sopt.haphap.domain.registration.dto;

import java.time.LocalDateTime;

public interface RegistrationFeedProjection {
    String getStage();              // 전형명 (예: 서류, 1차 면접)
    String getNickName();           // 익명 이름 (anonymousName)
    LocalDateTime getFeedCreatedAt(); // 등록/수정 시각 (updatedAt)
}