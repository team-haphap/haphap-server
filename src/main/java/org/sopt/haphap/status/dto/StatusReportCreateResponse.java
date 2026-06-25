package org.sopt.haphap.status.dto;

public record StatusReportCreateResponse(
        Long statusReportId,
        Long realtimeEventId
) {
}