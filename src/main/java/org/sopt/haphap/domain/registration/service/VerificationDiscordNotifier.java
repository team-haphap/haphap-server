package org.sopt.haphap.domain.registration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.registration.domain.RegistrationResult;
import org.sopt.haphap.domain.registration.event.RegistrationCreatedEvent;
import org.sopt.haphap.domain.registration.security.VerificationDecisionType;
import org.sopt.haphap.domain.registration.security.VerificationLinkSigner;
import org.sopt.haphap.domain.verification.entity.VerificationImage;
import org.sopt.haphap.domain.verification.repository.VerificationImageRepository;
import org.sopt.haphap.global.discord.DiscordWebhookClient;
import org.sopt.haphap.global.s3.S3PresignedUrlGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationDiscordNotifier {

    @Value("${app.base-url}")
    private String baseUrl;

    private final VerificationLinkSigner linkSigner;
    private final VerificationImageRepository verificationImageRepository;
    private final S3PresignedUrlGenerator presignedUrlGenerator;
    private final DiscordWebhookClient discordWebhookClient;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRegistrationCreated(RegistrationCreatedEvent e) {
        if (e.result() != RegistrationResult.PASS) {
            return;
        }
        try {
            notify(e.registrationId());
        } catch (Exception ex) {
            // 에러 삼키고 로그 남기도록
            log.warn("합격 인증 디스코드 알림 발송 실패 registrationId={}", e.registrationId(), ex);
        }
    }

    private void notify(Long registrationId) {
        List<String> imageUrls = verificationImageRepository.findByRegistrationId(registrationId).stream()
                .map(image -> presignedUrlGenerator.generateGetUrl(image.getS3Key()))
                .toList();

        String approveLink = confirmLink(registrationId, VerificationDecisionType.APPROVE);
        String rejectLink = confirmLink(registrationId, VerificationDecisionType.REJECT);

        discordWebhookClient.sendVerificationRequest(registrationId, imageUrls, approveLink, rejectLink);
    }

    private String confirmLink(Long registrationId, VerificationDecisionType action) {
        String token = linkSigner.issue(registrationId, action);
        return baseUrl + "/admin/registrations/verification?token=" + token;
    }
}