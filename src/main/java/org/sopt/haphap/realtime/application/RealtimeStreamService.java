package org.sopt.haphap.realtime.application;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.announcement.repository.AnnouncementRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.sopt.haphap.realtime.code.RealtimeErrorCode;
import org.sopt.haphap.realtime.dto.RealtimeEventResponse;
import org.sopt.haphap.realtime.infra.SseEmitterRepository;
import org.sopt.haphap.realtime.repository.RealtimeEventRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RealtimeStreamService {

    private static final int MISSED_EVENT_LIMIT = 100;

    private final AnnouncementRepository announcementRepository;
    private final RealtimeEventRepository realtimeEventRepository;
    private final SseEmitterRepository sseEmitterRepository;

    public SseEmitter connect(Long announcementId, Long lastEventId) {
        if (!announcementRepository.existsById(announcementId)) {
            throw new CustomException(RealtimeErrorCode.ANNOUNCEMENT_NOT_FOUND);
        }

        SseEmitter emitter = sseEmitterRepository.save(announcementId);
        sseEmitterRepository.sendConnected(emitter, announcementId);
        sendMissedEvents(emitter, announcementId, lastEventId);
        return emitter;
    }

    private void sendMissedEvents(SseEmitter emitter, Long announcementId, Long lastEventId) {
        if (lastEventId == null) {
            return;
        }

        realtimeEventRepository
                .findByAnnouncementIdAndIdGreaterThanOrderByIdAsc(
                        announcementId,
                        lastEventId,
                        PageRequest.of(0, MISSED_EVENT_LIMIT)
                )
                .stream()
                .map(RealtimeEventResponse::from)
                .forEach(response -> send(emitter, response));
    }

    private void send(SseEmitter emitter, RealtimeEventResponse response) {
        try {
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(response.eventId()))
                    .name(response.type())
                    .data(response));
        } catch (IOException | IllegalStateException ignored) {
            emitter.completeWithError(ignored);
        }
    }
}