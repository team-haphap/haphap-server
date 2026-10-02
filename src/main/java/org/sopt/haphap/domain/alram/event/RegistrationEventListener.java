package org.sopt.haphap.domain.alram.event;

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

@Slf4j
@Component
@RequiredArgsConstructor
public class RegistrationEventListener {

    private final AlramService alramService;
    private final AlramFailureRecorder alramFailureRecorder;
    private final AlramDispatcher alramDispatcher;

    // 합격 인증 승인 커밋 이후에만 발송 (롤백되면 알림도 안 나가야 하므로 AFTER_COMMIT)
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRegistrationApproved(RegistrationApprovedEvent event) {
        try {
            AlramDispatch dispatch = alramService.prepareStagePassedAlrams(event);   // 트랜잭션
            if (dispatch.isEmpty()) {
                return;
            }
            alramDispatcher.dispatch(event.postingId(), event.registrantUserId(), null, dispatch);   // 트랜잭션 밖 + 재시도
        } catch (Exception e) {
            // DB 자체가 실패한 경우
            log.error("합격 알람 준비 실패 - postingId={}, stageId={}, registrant={}",
                    event.postingId(), event.stageId(), event.registrantUserId(), e);
            alramFailureRecorder.record(event.postingId(), event.registrantUserId(), null, e);
        }
    }
}
