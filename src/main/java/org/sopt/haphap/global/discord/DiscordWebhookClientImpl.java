package org.sopt.haphap.global.discord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Value("${discord.webhook.mention-user-ids}")
    private String mentionUserIds;   // 예: "123456789012345678,987654321098765432" (Discord 유저 ID, 콤마 구분)

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

        String content = buildMentionContent();

        return Map.of(
                "content", content,
                "embeds", embeds
        );
    }

    private String buildMentionContent() {
        if (mentionUserIds == null || mentionUserIds.isBlank()) {
            return "";
        }
        return Arrays.stream(mentionUserIds.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .map(id -> "<@%s>".formatted(id))
                .collect(Collectors.joining(" "));
    }
}
