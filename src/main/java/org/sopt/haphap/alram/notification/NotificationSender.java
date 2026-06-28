package org.sopt.haphap.alram.notification;

public interface NotificationSender {
    void send(String fcmToken, NotificationMessage message);
}