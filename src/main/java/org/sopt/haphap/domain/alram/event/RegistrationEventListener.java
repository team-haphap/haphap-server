package org.sopt.haphap.domain.alram.event;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.alram.dispatch.AlramDispatch;
import org.sopt.haphap.domain.alram.dispatch.AlramDispatcher;
import org.sopt.haphap.domain.alram.service.AlramFailureRecorder;
import org.sopt.haphap.domain.alram.service.AlramService;
import org.sopt.haphap.domain.registration.event.RegistrationApprovedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegistrationEventListener {

    private final AlramService alramService;
    private final AlramFailureRecorder alramFailureRecorder;
    private final AlramDispatcher alramDispatcher;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRegistrationApproved(RegistrationApprovedEvent event) {
        try {
            List<AlramDispatch> dispatches = alramService.prepareStagePassedAlrams(event);
            if (dispatches.isEmpty()) {
                return;
            }
            for (AlramDispatch dispatch : dispatches) {
                if (!dispatch.isEmpty()) {
                    alramDispatcher.dispatch(event.postingId(), event.registrantUserId(), null, dispatch);
                }
            }
        } catch (Exception e) {
            log.error("합격 알람 준비 실패 - postingId={}, stageId={}, registrant={}",
                    event.postingId(), event.stageId(), event.registrantUserId(), e);
            alramFailureRecorder.record(event.postingId(), event.registrantUserId(), null, e);
        }
    }
}