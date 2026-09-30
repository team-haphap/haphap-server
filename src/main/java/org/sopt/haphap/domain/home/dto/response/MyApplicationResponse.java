package org.sopt.haphap.domain.home.dto.response;

public record MyApplicationResponse(
        Long postingId,
        String companyName,
        String title,
        String position,
        String currentStageStatus,   // 예: "서류 발표 중"
        String dDayLabel,            // 예: "D-2" / "D-day" / "발표 확인 중"
        String logoImageUrl
) {}
