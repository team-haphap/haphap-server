package org.sopt.haphap.domain.alram.notification;

public interface NotificationSender {
    void send(String fcmToken, NotificationMessage message);
}