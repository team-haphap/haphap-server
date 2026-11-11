package org.sopt.haphap.domain.alram.notification;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import org.sopt.haphap.domain.alram.exception.InvalidTokenException;
import org.sopt.haphap.domain.alram.exception.NotificationDeliveryException;
import org.sopt.haphap.domain.alram.exception.NotificationException;
import org.sopt.haphap.domain.alram.exception.RetryableNotificationException;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("prod")
public class FcmNotificationSender implements NotificationSender {

    @Override
    @Retryable(
            retryFor = RetryableNotificationException.class,   // 일시적 오류만 재시도
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)   // 1초 → 2초 → 4초
    )
    public void send(String fcmToken, NotificationMessage message) {
        try {
            // 1. 기본 data payload dynamic composition Map 구성
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("title", message.title());
            dataMap.put("body", message.body());
            dataMap.put("postingId", String.valueOf(message.postingId()));

            // 2. registrationId 존재 여부에 따른 조건부 키-값 추가
            if (message.registrationId() != null) {
                dataMap.put("type", "PASS_CARD");
                dataMap.put("registrationId", String.valueOf(message.registrationId()));
            }

            // 3. FCM Message 객체 생성
            Message fcmMessage = Message.builder()
                    .setToken(fcmToken)
                    .putAllData(dataMap)
                    .build();

            String messageId = FirebaseMessaging.getInstance().send(fcmMessage);
            log.info("[FCM 발송 성공] messageId={}", messageId);
        } catch (FirebaseMessagingException e) {
            throw classify(e, fcmToken);   // 예외를 의미별로 분류해 다시 던짐
        }
    }

    // FCM 에러 코드를 우리 도메인 예외로
    private NotificationException classify(FirebaseMessagingException e, String token) {
        MessagingErrorCode code = e.getMessagingErrorCode();
        log.warn("[FCM 발송 실패] code={}, token={}, message={}", code, token, e.getMessage());

        if (code == null) {
            return new NotificationDeliveryException("알 수 없는 FCM 오류", e);
        }
        return switch (code) {
            // 일시적오류 → 재시도
            case UNAVAILABLE, INTERNAL, QUOTA_EXCEEDED ->
                    new RetryableNotificationException("일시적 FCM 오류: " + code, e);
            // 토큰 자체가 죽음 → 비활성화
            case UNREGISTERED, SENDER_ID_MISMATCH ->
                    new InvalidTokenException("유효하지 않은 토큰: " + code, e);
            // 그 외(설정 오류 등) → 재시도 없이 포기
            default ->
                    new NotificationDeliveryException("FCM 발송 실패: " + code, e);
        };
    }

    // 재시도 3번을 모두 실패시
    @Recover
    public void recover(RetryableNotificationException e, String fcmToken, NotificationMessage message) {
        log.error("[FCM 재시도 소진] token={}, 최종 발송 실패", fcmToken, e);
        throw new NotificationDeliveryException("재시도 소진 후 발송 실패", e);
    }
}
//Map.of가 불변 객체여서 조건문 동적 insert가 불가한 것 같아서 HashMap 선언해서 기본 데이터 넣어놓고 if문으로 넣어놨습니다