package org.sopt.haphap.realtime.application;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.announcement.repository.AnnouncementRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.sopt.haphap.realtime.code.RealtimeErrorCode;
import org.sopt.haphap.realtime.dto.RealtimeEventResponse;
import org.sopt.haphap.realtime.dto.RealtimeFeedResponse;
import org.sopt.haphap.realtime.dto.RealtimeSummaryResponse;
import org.sopt.haphap.realtime.repository.RealtimeEventRepository;
import org.sopt.haphap.status.repository.StatusReportRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RealtimeQueryService {

    private static final int MAX_FEED_SIZE = 50;
    private static final int DEFAULT_DETAIL_EVENT_SIZE = 20;

    private final AnnouncementRepository announcementRepository;
    private final RealtimeEventRepository realtimeEventRepository;
    private final StatusReportRepository statusReportRepository;

    public RealtimeFeedResponse getFeed(Long cursorId, int size) {
        int pageSize = Math.min(Math.max(size, 1), MAX_FEED_SIZE);
        List<RealtimeEventResponse> events = realtimeEventRepository.findFeed(cursorId, PageRequest.of(0, pageSize))
                .stream()
                .map(RealtimeEventResponse::from)
                .toList();

        return RealtimeFeedResponse.of(events);
    }

    public RealtimeSummaryResponse getAnnouncementSummary(Long announcementId) {
        if (!announcementRepository.existsById(announcementId)) {
            throw new CustomException(RealtimeErrorCode.ANNOUNCEMENT_NOT_FOUND);
        }

        List<RealtimeEventResponse> latestEvents = realtimeEventRepository
                .findLatestByAnnouncementId(announcementId, PageRequest.of(0, DEFAULT_DETAIL_EVENT_SIZE))
                .stream()
                .map(RealtimeEventResponse::from)
                .toList();

        return new RealtimeSummaryResponse(
                announcementId,
                statusReportRepository.countByAnnouncementId(announcementId),
                latestEvents
        );
    }
}