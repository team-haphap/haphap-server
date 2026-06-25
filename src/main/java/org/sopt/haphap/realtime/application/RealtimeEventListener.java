package org.sopt.haphap.realtime.application;


import lombok.RequiredArgsConstructor;
import org.sopt.haphap.realtime.domain.RealtimeEvent;
import org.sopt.haphap.realtime.dto.RealtimeEventResponse;
import org.sopt.haphap.realtime.infra.SseEmitterRepository;
import org.sopt.haphap.realtime.repository.RealtimeEventRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RealtimeEventListener {

    private final RealtimeEventRepository realtimeEventRepository;
    private final SseEmitterRepository sseEmitterRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void send(RealtimeEventCreatedEvent event) {
        RealtimeEvent realtimeEvent = realtimeEventRepository.findById(event.realtimeEventId())
                .orElse(null);

        if (realtimeEvent == null) {
            return;
        }

        sseEmitterRepository.sendToAnnouncement(
                realtimeEvent.getAnnouncement().getId(),
                RealtimeEventResponse.from(realtimeEvent)
        );
    }
}