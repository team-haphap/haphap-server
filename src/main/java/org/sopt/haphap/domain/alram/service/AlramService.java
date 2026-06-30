package org.sopt.haphap.domain.alram.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.alram.code.AlramErrorCode;
import org.sopt.haphap.domain.alram.domain.Alram;
import org.sopt.haphap.domain.alram.domain.AlramSetting;
import org.sopt.haphap.domain.alram.domain.AlramType;
import org.sopt.haphap.domain.alram.domain.PushToken;
import org.sopt.haphap.domain.alram.notification.NotificationMessage;
import org.sopt.haphap.domain.alram.notification.NotificationSender;
import org.sopt.haphap.domain.alram.repository.AlramRepository;
import org.sopt.haphap.domain.alram.repository.AlramSettingRepository;
import org.sopt.haphap.domain.alram.repository.PushTokenRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.registration.event.RegistrationCreatedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlramService {

    private final PostingRepository postingRepository;
    private final AlramSettingRepository alramSettingRepository;
    private final AlramRepository alramRepository;
    private final PushTokenRepository pushTokenRepository;
    private final NotificationSender notificationSender;

    // 비동기 스레드에서 호출되므로 여기서 새 트랜잭션을 연다.
    @Transactional
    public void notifySubscribers(RegistrationCreatedEvent event) {
        List<AlramSetting> subscribers = alramSettingRepository
                .findActiveSubscribers(event.postingId(), event.registrantUserId());

        if (subscribers.isEmpty()) {
            log.debug("알람 수신 대상 없음 - postingId={}", event.postingId());
            return;
        }

        Posting posting = postingRepository.findById(event.postingId())
                .orElseThrow(() -> new CustomException(AlramErrorCode.POSTING_NOT_FOUND));
        NotificationMessage message = createMessage(posting, event.stage());

        // 1) 알람 여부 동의한 userId 수집
        List<Long> userIds = subscribers.stream()
                .map(s -> s.getUser().getId())
                .toList();

        // 2) 토큰을 한 번에 조회 후 userId 기준으로 그룹핑
        Map<Long, List<PushToken>> tokensByUserId = pushTokenRepository
                .findAllByUserIdInAndActiveTrue(userIds).stream()
                .collect(Collectors.groupingBy(token -> token.getUser().getId()));

        // 3) 알람 내역 저장 + 발송
        for (AlramSetting subscriber : subscribers) {
            User receiver = subscriber.getUser();
            alramRepository.save(Alram.create(receiver, posting, AlramType.STAGE_REGISTERED,
                    message.title(), message.body()));

            List<PushToken> tokens = tokensByUserId.getOrDefault(receiver.getId(), List.of());
            tokens.forEach(token -> notificationSender.send(token.getFcmToken(), message));
        }
        /*

        for (AlramSetting subscriber : subscribers) {
            User receiver = subscriber.getUser();
            alramRepository.save(Alram.create(receiver, posting, AlramType.STAGE_REGISTERED,message.title(), message.body()));


         */
    }

    private NotificationMessage createMessage(Posting posting, String stage) {
        String title = "전형 소식 알림";
        String body = String.format("'%s' 공고의 '%s' 전형 소식이 등록되었어요.",
                posting.getTitle(), stage);
        return new NotificationMessage(title, body);
    }

    private void pushToAllDevices(User receiver, NotificationMessage message) {
        List<PushToken> tokens = pushTokenRepository.findByUserIdAndActiveTrue(receiver.getId());
        if (tokens.isEmpty()) {
            log.debug("활성 푸시 토큰 없음 - memberId={}", receiver.getId());
            return;
        }
        tokens.forEach(token -> notificationSender.send(token.getFcmToken(), message));
    }
}