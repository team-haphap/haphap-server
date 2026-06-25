package org.sopt.haphap.realtime.dto;

import java.util.List;

public record RealtimeSummaryResponse(
        Long announcementId,
        long reportCount,
        List<RealtimeEventResponse> latestEvents
) {
}