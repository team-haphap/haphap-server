package org.sopt.haphap.global.discord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordWebhookClientImpl implements DiscordWebhookClient {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);
    private static final int EMBED_COLOR = 0x5865F2;

    private final WebClient webClient;

    @Value("${discord.webhook.verification-url}")
    private String webhookUrl;

    @Override
    public void sendVerificationRequest(Long registrationId, List<String> imageUrls,
                                        String approveLink, String rejectLink) {
        Map<String, Object> payload = buildPayload(registrationId, imageUrls, approveLink, rejectLink);

        webClient.post()
                .uri(webhookUrl)
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .timeout(REQUEST_TIMEOUT)
                .onErrorMap(e -> new IllegalStateException("디스코드 알림 발송 실패", e))
                .block();
    }

    // 여러 이미지를 discord embed로 보낼 때, 같은 url을 가진 embed끼리는
    // 디스코드가 자동으로 하나의 갤러리처럼 묶어서 보여주도록
    private Map<String, Object> buildPayload(Long registrationId, List<String> imageUrls,
                                             String approveLink, String rejectLink) {
        List<Map<String, Object>> embeds = new ArrayList<>();

        embeds.add(Map.of(
                "title", "합격 인증 검토 요청",
                "description", "등록 #%d 건의 인증샷을 확인해주세요.\n[승인하기](%s) · [반려하기](%s)"
                        .formatted(registrationId, approveLink, rejectLink),
                "color", EMBED_COLOR
        ));
        for (String imageUrl : imageUrls) {
            embeds.add(Map.of("url", approveLink, "image", Map.of("url", imageUrl)));
        }

        return Map.of("embeds", embeds);
    }
}