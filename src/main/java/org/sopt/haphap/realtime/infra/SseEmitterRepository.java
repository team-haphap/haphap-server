package org.sopt.haphap.realtime.infra;


import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.realtime.dto.RealtimeEventResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class SseEmitterRepository {

    private static final long TIMEOUT_MILLIS = 30 * 60 * 1000L;

    private final ConcurrentMap<Long, Set<SseEmitter>> emittersByAnnouncementId = new ConcurrentHashMap<>();

    public SseEmitter save(Long announcementId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MILLIS);
        emittersByAnnouncementId
                .computeIfAbsent(announcementId, ignored -> ConcurrentHashMap.newKeySet())
                .add(emitter);

        emitter.onCompletion(() -> remove(announcementId, emitter));
        emitter.onTimeout(() -> remove(announcementId, emitter));
        emitter.onError(error -> remove(announcementId, emitter));
        return emitter;
    }

    public void sendToAnnouncement(Long announcementId, RealtimeEventResponse response) {
        Set<SseEmitter> emitters = emittersByAnnouncementId.getOrDefault(announcementId, Set.of());

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .id(String.valueOf(response.eventId()))
                        .name(response.type())
                        .data(response));
            } catch (IOException | IllegalStateException exception) {
                log.warn("Failed to send SSE. announcementId={}, eventId={}", announcementId, response.eventId(), exception);
                remove(announcementId, emitter);
            }
        }
    }

    public void sendConnected(SseEmitter emitter, Long announcementId) {
        try {
            emitter.send(SseEmitter.event()
                    .name("CONNECTED")
                    .data("announcement stream connected: " + announcementId));
        } catch (IOException | IllegalStateException exception) {
            remove(announcementId, emitter);
        }
    }

    private void remove(Long announcementId, SseEmitter emitter) {
        Set<SseEmitter> emitters = emittersByAnnouncementId.get(announcementId);
        if (emitters == null) {
            return;
        }

        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emittersByAnnouncementId.remove(announcementId);
        }
    }
}