package org.sopt.haphap.domain.alram.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import org.sopt.haphap.domain.alram.repository.AlramRepository;
import org.sopt.haphap.domain.alram.repository.AlramSettingRepository;
import org.sopt.haphap.domain.alram.repository.PushTokenRepository;
import org.sopt.haphap.domain.posting.code.PostingErrorCode;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.registration.event.RegistrationApprovedEvent;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.user.repository.UserRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlramService {

    private final PostingRepository postingRepository;
    private final PostingStageRepository postingStageRepository;
    private final AlramSettingRepository alramSettingRepository;
    private final AlramRepository alramRepository;
    private final PushTokenRepository pushTokenRepository;
    private final UserRepository userRepository;

    // 트랜잭션 안: (공고x전형) 최초 합격 인증 승인 1건만 - 구독자 조회 + 알람 내역 저장 + 발송 대상 수집.
    @Transactional
    public List<AlramDispatch> prepareStagePassedAlrams(RegistrationApprovedEvent event) {
        Posting posting = postingRepository.findById(event.postingId())
                .orElseThrow(() -> new CustomException(AlramErrorCode.POSTING_NOT_FOUND));

        if (posting.isClosed()) {
            log.info("합격 알람 스킵(마감) - postingId={}", event.postingId());
            return List.of();
        }

        PostingStage stage = postingStageRepository.findById(event.stageId())
                .orElseThrow(() -> new CustomException(PostingErrorCode.STAGE_NOT_FOUND));

        List<AlramDispatch> dispatches = new ArrayList<>();

        // 공고 구독자 알림 — (공고x전형) 최초 1회, registrationId 없음
        if (stage.getPassAlramSentAt() == null) {
            stage.markPassAlramSent(LocalDateTime.now());

            List<AlramSetting> subscribers = alramSettingRepository.findActiveSubscribers(event.postingId());
            if (!subscribers.isEmpty()) {
                NotificationMessage subscriberMsg = createStagePassedMessage(posting, stage);
                dispatches.add(buildDispatch(posting, subscribers, AlramType.STAGE_PASSED, subscriberMsg));
            }
        } else {
            log.info("합격 알람 스킵(이미 발송됨) - postingId={}, stageId={}", event.postingId(), stage.getId());
        }

        // 승인된 본인 알림 — 매 승인마다, registrationId 포함
        dispatches.add(buildPassCardDispatch(posting, stage, event));

        return dispatches;
    }

    // 트랜잭션 안: 공고 최초 마감 1회만 - 구독자 조회 + 알람 내역 저장 + 발송 대상 수집.
    @Transactional
    public AlramDispatch prepareClosedAlrams(Long postingId) {
        Posting posting = postingRepository.findById(postingId)
                .orElseThrow(() -> new CustomException(AlramErrorCode.POSTING_NOT_FOUND));

        if (posting.getClosedAlramSentAt() != null) {
            log.info("마감 알람 스킵(이미 발송됨) - postingId={}", postingId);
            return AlramDispatch.empty();
        }

        posting.markClosedAlramSent(LocalDateTime.now());

        List<AlramSetting> subscribers = alramSettingRepository.findActiveSubscribers(postingId);
        if (subscribers.isEmpty()) {
            log.info("마감 알람 수신 대상 없음 - postingId={}", postingId);
            return AlramDispatch.empty();
        }

        NotificationMessage message = createClosedMessage(posting);
        return buildDispatch(posting, subscribers, AlramType.POSTING_CLOSED, message);
    }

    private AlramDispatch buildDispatch(
            Posting posting, List<AlramSetting> subscribers, AlramType type, NotificationMessage message) {
        List<Long> userIds = subscribers.stream()
                .map(s -> s.getUser().getId())
                .toList();

        Map<Long, List<PushToken>> tokensByUserId = pushTokenRepository
                .findAllByUserIdInAndActiveTrue(userIds).stream()
                .collect(Collectors.groupingBy(token -> token.getUser().getId()));

        List<SendTarget> targets = subscribers.stream()
                .flatMap(subscriber -> {
                    User receiver = subscriber.getUser();
                    // 인앱 알람 내역은 푸시 성공 여부와 무관하게 저장
                    alramRepository.save(Alram.create(receiver, posting, type, message.title(), message.body()));
                    // 트랜잭션 밖에서 쓸 토큰 값만 복사
                    return tokensByUserId.getOrDefault(receiver.getId(), List.of()).stream()
                            .map(token -> new SendTarget(token.getId(), token.getFcmToken()));
                })
                .toList();

        return new AlramDispatch(message, targets);
    }

    // 구독자용 — registrationId 없음
    private NotificationMessage createStagePassedMessage(Posting posting, PostingStage stage) {
        String title = "[%s] 합격 소식이 도착했어요".formatted(posting.getCompany().getName());
        String body = "%s %s 전형 현황이 업데이트됐어요. 지금 확인해보세요.".formatted(
                posting.getPosition(),
                stage.getName()
        );
        return new NotificationMessage(title, body, posting.getId()); // registrationId null
    }

    // 본인용 합격카드 알림
    private AlramDispatch buildPassCardDispatch(Posting posting, PostingStage stage, RegistrationApprovedEvent event) {
        String title = "[%s] 합격카드가 도착했어요 🎉".formatted(posting.getCompany().getName());
        String body = "%s %s 전형을 통과했어요! 합격카드를 확인해보세요.".formatted(
                posting.getPosition(),
                stage.getName()
        );
        NotificationMessage msg = new NotificationMessage(title, body, posting.getId(), event.registrationId());

        List<PushToken> tokens = pushTokenRepository
                .findAllByUserIdInAndActiveTrue(List.of(event.registrantUserId()));

        // 인앱 알람 내역 저장
        User receiver = tokens.isEmpty() ? null : tokens.get(0).getUser();
        if (receiver != null) {
            alramRepository.save(Alram.create(receiver, posting, AlramType.PASS_CARD, msg.title(), msg.body()));
        } else {
            userRepository.findById(event.registrantUserId()).ifPresent(user ->
                    alramRepository.save(Alram.create(user, posting, AlramType.PASS_CARD, msg.title(), msg.body()))
            );
        }

        List<SendTarget> targets = tokens.stream()
                .map(t -> new SendTarget(t.getId(), t.getFcmToken()))
                .toList();

        return new AlramDispatch(msg, targets);
    }

    private NotificationMessage createClosedMessage(Posting posting) {
        String title = "[%s]이 마감됐어요".formatted(posting.getTitle());
        String body = "모든 전형이 마무리됐어요.";
        return new NotificationMessage(title, body, posting.getId());
    }
}
