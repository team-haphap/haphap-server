package org.sopt.haphap.domain.posting.dto.response;

import org.sopt.haphap.domain.registration.domain.RegistrationResult;

import java.time.LocalDateTime;
import java.util.List;

public record PostingDetailResponse(
        String companyName,
        String postingTitle,
        String category,
        String location,
        String position,
        String currentState,
        String companyImageUrl,// 현재 진행 전형 (없으면 null)
        SummaryResponse summary,
        List<RegistrationFeedResponse> registrations
) {
    public record SummaryResponse(
            long registeredCount,
            List<String> profileImages,       // 최근 4명
            long additionalParticipantCount   // registeredCount - 4 (음수면 0)
    ) {}

    public record RegistrationFeedResponse(
            String stage,
            String nickName,
            RegistrationResult registrationResult,
            LocalDateTime feedCreatedAt
    ) {}
}