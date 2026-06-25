package org.sopt.haphap.realtime.application;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RealtimeEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publishAfterCommit(Long realtimeEventId) {
        publisher.publishEvent(new RealtimeEventCreatedEvent(realtimeEventId));
    }
}
