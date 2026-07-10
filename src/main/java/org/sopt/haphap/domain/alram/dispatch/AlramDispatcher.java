package org.sopt.haphap.domain.alram.dispatch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.alram.exception.InvalidTokenException;
import org.sopt.haphap.domain.alram.exception.NotificationDeliveryException;
import org.sopt.haphap.domain.alram.exception.RetryableNotificationException;
import org.sopt.haphap.domain.alram.notification.NotificationSender;
import org.sopt.haphap.domain.alram.service.AlramFailureRecorder;
import org.sopt.haphap.domain.alram.service.PushTokenService;
import org.sopt.haphap.domain.registration.event.RegistrationCreatedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlramDispatcher {

    private final NotificationSender notificationSender;
    private final PushTokenService pushTokenService;
    private final AlramFailureRecorder failureRecorder;

    // 트랜잭션 밖: 토큰별 발송 + 재시도/실패 분기
    public void dispatch(RegistrationCreatedEvent event, AlramDispatch dispatch) {
        for (SendTarget target : dispatch.targets()) {
            try {
                notificationSender.send(target.fcmToken(), dispatch.message());
            } catch (InvalidTokenException e) {
                log.warn("[알람] 무효 토큰 비활성화 - tokenId={}", target.tokenId());
                pushTokenService.deactivate(target.tokenId());
            }
            catch (RetryableNotificationException e) {
                log.error("[알람] 재시도 소진 후 실패 - tokenId={}", target.tokenId(), e);
                failureRecorder.record(event, e);
            }
            catch (NotificationDeliveryException e) {
                log.error("[알람] 발송 최종 실패 - tokenId={}", target.tokenId(), e);
                failureRecorder.record(event, e);
            }
            catch (Exception e) {   // 예상 못한 예외로 루프 전체가 죽는 것 방지
                log.error("[알람] 예상치 못한 발송 오류 - tokenId={}", target.tokenId(), e);
                failureRecorder.record(event, e);
            }
        }
    }
}