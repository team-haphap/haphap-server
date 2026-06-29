package org.sopt.haphap.domain.alram.exception;

// 토큰이 죽음 → 재시도 무의미, 토큰을 비활성화
public class InvalidTokenException extends NotificationException {
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}