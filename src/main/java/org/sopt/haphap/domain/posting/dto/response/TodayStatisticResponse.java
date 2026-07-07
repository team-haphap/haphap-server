package org.sopt.haphap.domain.posting.dto.response;

public record TodayStatisticResponse(
        long cumulatedCount,
        long onGoingCount,
        long announcedCount
) {}