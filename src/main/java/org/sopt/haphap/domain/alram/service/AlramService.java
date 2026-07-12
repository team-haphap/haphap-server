package org.sopt.haphap.domain.alram.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.alram.code.AlramErrorCode;
import org.sopt.haphap.domain.alram.dispatch.AlramDispatch;
import org.sopt.haphap.domain.alram.dispatch.SendTarget;
import org.sopt.haphap.domain.alram.domain.Alram;
import org.sopt.haphap.domain.alram.domain.AlramSetting;
import org.sopt.haphap.domain.alram.domain.AlramType;
import org.sopt.haphap.domain.alram.domain.PushToken;
import org.sopt.haphap.domain.alram.notification.NotificationMessage;
import org.sopt.haphap.domain.alram.notification.NotificationSender;
import org.sopt.haphap.domain.alram.repository.AlramRepository;
import org.sopt.haphap.domain.alram.repository.AlramSettingRepository;
import org.sopt.haphap.domain.alram.repository.PushTokenRepository;
import org.sopt.haphap.domain.posting.service.calculator.CurrentStageResolver;
import org.sopt.haphap.domain.registration.domain.RegistrationResult;
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
    private final CurrentStageResolver currentStageResolver;

    // 트랜잭션 안: 구독자 조회 + 알람 내역 저장 + 발송 대상(토큰) 수집까지
    @Transactional
    public AlramDispatch prepareAlrams(RegistrationCreatedEvent event) {

        String currentStage = currentStageResolver.resolveCurrentState(event.postingId());

        if (currentStage == null) {
            return AlramDispatch.empty();
        }

        if (!currentStage.equals(event.stage())) {
            log.debug(
                    "현재 진행 전형이 아니므로 알람 미발송 - postingId={}, stage={}, currentStage={}",
                    event.postingId(),
                    event.stage(),
                    currentStage
            );

            return AlramDispatch.empty();
        }

        List<AlramSetting> subscribers = alramSettingRepository
                .findActiveSubscribers(event.postingId(), event.registrantUserId());

        if (subscribers.isEmpty()) {
            log.debug("알람 수신 대상 없음 - postingId={}", event.postingId());
            return AlramDispatch.empty();
        }

        Posting posting = postingRepository.findById(event.postingId())
                .orElseThrow(() -> new CustomException(AlramErrorCode.POSTING_NOT_FOUND));
        NotificationMessage message = createMessage(posting, event.stage(),event.result());

        // 알람 여부 동의한 userId 수집
        List<Long> userIds = subscribers.stream()
                .map(s -> s.getUser().getId())
                .toList();

        // 토큰을 한 번에 조회 후 userId 기준으로 그룹핑
        Map<Long, List<PushToken>> tokensByUserId = pushTokenRepository
                .findAllByUserIdInAndActiveTrue(userIds).stream()
                .collect(Collectors.groupingBy(token -> token.getUser().getId()));

        // 알람 내역 저장 + 발송 대상 수집
        List<SendTarget> targets = subscribers.stream()
                .flatMap(subscriber -> {
                    User receiver = subscriber.getUser();
                    // 인앱 알람 내역은 푸시 성공 여부와 무관하게 저장
                    alramRepository.save(Alram.create(receiver, posting,
                            AlramType.STAGE_REGISTERED, message.title(), message.body()));
                    // 트랜잭션 밖에서 쓸 토큰 값만 복사
                    return tokensByUserId.getOrDefault(receiver.getId(), List.of()).stream()
                            .map(token -> new SendTarget(token.getId(), token.getFcmToken()));
                })
                .toList();

        return new AlramDispatch(message, targets);
    }

    private NotificationMessage createMessage(Posting posting, String stage, RegistrationResult result) {
        String title = "전형 소식 알림";
        String body = String.format("%s %s의 %s %s가 등록중",
                posting.getCompany().getName(),   // 기업명
                posting.getTitle(),               // 공고명
                stage,                            // 전형
                result.getDescription());         // 결과 (합격/불합격/대기)
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