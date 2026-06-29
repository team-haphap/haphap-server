package org.sopt.haphap.domain.alram.exception;

// 일시적 오류 → 재시도 대상 (FCM 서버 다운, 타임아웃, 쿼터 초과 등)
public class RetryableNotificationException extends NotificationException {
    public RetryableNotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}