package org.sopt.haphap.realtime.dto;


import org.sopt.haphap.realtime.domain.RealtimeEvent;

import java.time.LocalDateTime;

public record RealtimeEventResponse(
        Long eventId,
        Long announcementId,
        String companyName,
        String announcementTitle,
        String type,
        String stage,
        String stageName,
        String result,
        String resultName,
        String title,
        String body,
        LocalDateTime createdAt
) {

    public static RealtimeEventResponse from(RealtimeEvent event) {
        return new RealtimeEventResponse(
                event.getId(),
                event.getAnnouncement().getId(),
                event.getAnnouncement().getCompanyName(),
                event.getAnnouncement().getTitle(),
                event.getType().name(),
                event.getStage().name(),
                event.getStage().getDisplayName(),
                event.getResult().name(),
                event.getResult().getDisplayName(),
                event.getTitle(),
                event.getBody(),
                event.getCreatedAt()
        );
    }
}