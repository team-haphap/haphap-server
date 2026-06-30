package org.sopt.haphap.domain.alram.dispatch;

public record SendTarget(
        Long tokenId, String fcmToken
) {
}