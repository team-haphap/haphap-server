package org.sopt.haphap.global.discord;

import java.util.List;

public interface DiscordWebhookClient {
    void sendVerificationRequest(Long registrationId, List<String> imageUrls, String approveLink, String rejectLink);
}