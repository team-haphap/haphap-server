package org.sopt.haphap.domain.alram.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!prod")   // prod가 아닐 땐 항상 이게 주입됨
public class LoggingNotificationSender implements NotificationSender {

    @Override
    public void send(String fcmToken, NotificationMessage message) {
        log.info("[푸시 발송(MOCK)] token={}, title={}, body={}",
                fcmToken, message.title(), message.body());
    }
}