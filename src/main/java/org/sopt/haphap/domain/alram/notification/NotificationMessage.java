package org.sopt.haphap.domain.alram.notification;

public record NotificationMessage(
        String title,
        String body,
        Long postingId,
        Long registrationId //합격카드 진입
) {
    public NotificationMessage(String title, String body, Long postingId) {
        this(title, body, postingId, null);
    }
}