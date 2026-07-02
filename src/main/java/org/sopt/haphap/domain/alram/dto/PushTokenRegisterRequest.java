package org.sopt.haphap.domain.alram.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.sopt.haphap.domain.alram.domain.DeviceType;

public record PushTokenRegisterRequest(
        @NotBlank(message = "디바이스 ID는 필수입니다.")
        String deviceId,

        @NotBlank(message = "FCM 토큰은 필수입니다.")
        String fcmToken,

        @NotNull(message = "디바이스 타입은 필수입니다.")
        DeviceType deviceType
) {
}