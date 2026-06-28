package org.sopt.haphap.alram.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.alram.domain.AlramFailure;
import org.sopt.haphap.alram.repository.AlramFailureRepository;
import org.sopt.haphap.alram.service.AlramService;
import org.sopt.haphap.registration.event.RegistrationCreatedEvent;
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
    private final AlramFailureRepository alramFailureRepository;

    /*
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRegistrationCreated(RegistrationCreatedEvent event) {
        alramService.notifySubscribers(event);
    }

     */

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRegistrationCreated(RegistrationCreatedEvent event) {
        try {
            alramService.notifySubscribers(event);
        } catch (Exception e) {
            log.error("알람 발송 실패 - postingId={}, stage={}, registrant={}",
                    event.postingId(), event.stage(), event.registrantUserId(), e);
            saveFailure(event, e);
        }
    }

    // 알람 본 트랜잭션이 깨졌기 때문에, 실패 기록은 독립 트랜잭션으로 저장
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailure(RegistrationCreatedEvent event, Exception e) {
        alramFailureRepository.save(AlramFailure.from(
                event.postingId(), event.registrantUserId(), event.stage(), e));
    }
}