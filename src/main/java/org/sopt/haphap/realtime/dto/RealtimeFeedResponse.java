package org.sopt.haphap.realtime.dto;

import java.util.List;

public record RealtimeFeedResponse(
        List<RealtimeEventResponse> events,
        Long nextCursor
) {

    public static RealtimeFeedResponse of(List<RealtimeEventResponse> events) {
        Long nextCursor = events.isEmpty() ? null : events.get(events.size() - 1).eventId();
        return new RealtimeFeedResponse(events, nextCursor);
    }
}