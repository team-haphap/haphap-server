package org.sopt.haphap.domain.alram.exception;

public abstract class NotificationException extends RuntimeException {
    protected NotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}