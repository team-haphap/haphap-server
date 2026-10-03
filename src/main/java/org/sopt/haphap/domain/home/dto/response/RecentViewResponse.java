package org.sopt.haphap.domain.home.dto.response;

public record RecentViewResponse(
        Long postingId,
        String companyName,
        String title,
        //String position,
        String category,
        String nextStage,    // 다음 전형명 (없으면 null)
        String dDayLabel,    // 다음 전형 기준 D-day, 마감이면 "마감"
        String logoImageUrl
) {}
