package org.sopt.haphap.domain.alram.exception;

// 영구 실패 또는 재시도 모두 실패 →  실패 기록
public class NotificationDeliveryException extends NotificationException {
    public NotificationDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
