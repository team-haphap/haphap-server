package org.sopt.haphap.realtime.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.sopt.haphap.realtime.application.RealtimeQueryService;
import org.sopt.haphap.realtime.application.RealtimeStreamService;
import org.sopt.haphap.realtime.code.RealtimeSuccessCode;
import org.sopt.haphap.realtime.dto.RealtimeFeedResponse;
import org.sopt.haphap.realtime.dto.RealtimeSummaryResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/realtime")
public class RealtimeController {

    private final RealtimeQueryService realtimeQueryService;
    private final RealtimeStreamService realtimeStreamService;

    @GetMapping("/feed")
    public ResponseEntity<SuccessResponse<RealtimeFeedResponse>> getFeed(
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "20") int size
    ) {
        RealtimeFeedResponse response = realtimeQueryService.getFeed(cursorId, size);
        return ResponseEntity.ok(ApiResponse.success(RealtimeSuccessCode.REALTIME_FEED_FOUND, response));
    }

    @GetMapping("/announcements/{announcementId}/summary")
    public ResponseEntity<SuccessResponse<RealtimeSummaryResponse>> getAnnouncementSummary(
            @PathVariable Long announcementId
    ) {
        RealtimeSummaryResponse response = realtimeQueryService.getAnnouncementSummary(announcementId);
        return ResponseEntity.ok(ApiResponse.success(RealtimeSuccessCode.REALTIME_SUMMARY_FOUND, response));
    }

    @GetMapping(
            value = "/announcements/{announcementId}/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter connectAnnouncementStream(
            @PathVariable Long announcementId,
            @RequestHeader(value = "Last-Event-ID", required = false) Long lastEventId
    ) {
        return realtimeStreamService.connect(announcementId, lastEventId);
    }
}