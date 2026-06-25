package org.sopt.haphap.realtime.application;


import lombok.RequiredArgsConstructor;
import org.sopt.haphap.announcement.domain.Announcement;
import org.sopt.haphap.announcement.repository.AnnouncementRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.sopt.haphap.realtime.code.RealtimeErrorCode;
import org.sopt.haphap.realtime.domain.RealtimeEvent;
import org.sopt.haphap.realtime.repository.RealtimeEventRepository;
import org.sopt.haphap.status.domain.StatusReport;
import org.sopt.haphap.status.dto.StatusReportCreateRequest;
import org.sopt.haphap.status.dto.StatusReportCreateResponse;
import org.sopt.haphap.status.repository.StatusReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatusReportService {

    private final AnnouncementRepository announcementRepository;
    private final StatusReportRepository statusReportRepository;
    private final RealtimeEventRepository realtimeEventRepository;
    private final RealtimeEventPublisher realtimeEventPublisher;

    @Transactional
    public StatusReportCreateResponse create(Long memberId, StatusReportCreateRequest request) {
        Announcement announcement = announcementRepository.findById(request.announcementId())
                .orElseThrow(() -> new CustomException(RealtimeErrorCode.ANNOUNCEMENT_NOT_FOUND));

        StatusReport statusReport = StatusReport.create(
                memberId,
                announcement,
                request.stage(),
                request.notifiedDate(),
                request.notifiedTimeText(),
                request.notificationChannel(),
                request.result()
        );
        StatusReport savedStatusReport = statusReportRepository.save(statusReport);

        RealtimeEvent realtimeEvent = RealtimeEvent.statusReportCreated(
                announcement,
                request.stage(),
                request.result()
        );
        RealtimeEvent savedRealtimeEvent = realtimeEventRepository.save(realtimeEvent);

        realtimeEventPublisher.publishAfterCommit(savedRealtimeEvent.getId());

        return new StatusReportCreateResponse(savedStatusReport.getId(), savedRealtimeEvent.getId());
    }
}