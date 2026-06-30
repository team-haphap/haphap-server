package org.sopt.haphap.domain.alram.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.alram.service.AlramFailureRecorder;
import org.sopt.haphap.domain.alram.domain.AlramFailure;
import org.sopt.haphap.domain.alram.repository.AlramFailureRepository;
import org.sopt.haphap.domain.alram.service.AlramService;
import org.sopt.haphap.domain.registration.event.RegistrationCreatedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegistrationEventListener {

    private final AlramService alramService;
    private final AlramFailureRecorder alramFailureRecorder;
    private final AlramFailureRepository alramFailureRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRegistrationCreated(RegistrationCreatedEvent event) {
        try {
            alramService.notifySubscribers(event);
        } catch (Exception e) {
            log.error("알람 발송 실패 - postingId={}, stage={}, registrant={}",
                    event.postingId(), event.stage(), event.registrantUserId(), e);
            alramFailureRecorder.record(event, e);
        }
    }
}