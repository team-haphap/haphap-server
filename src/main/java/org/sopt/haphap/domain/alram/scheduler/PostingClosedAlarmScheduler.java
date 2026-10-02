package org.sopt.haphap.domain.alram.scheduler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.alram.dispatch.AlramDispatch;
import org.sopt.haphap.domain.alram.dispatch.AlramDispatcher;
import org.sopt.haphap.domain.alram.service.AlramFailureRecorder;
import org.sopt.haphap.domain.alram.service.AlramService;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 공고 최초 마감 알림. Posting.isClosed()는 순수 계산(최종합격 전형 movedAt + 4일)이라
 * "마감되는 바로 그 순간"을 알려주는 이벤트가 없다 - 그래서 매일 자정, 그 경계가 바뀌는 시점에 맞춰
 * 스캔한다(마감 기준 자체가 날짜/자정 단위라 더 자주 돌 필요가 없다).
 * 공고당 closedAlramSentAt에 1회만 기록 - 마감 이후 단순 데이터 정정으로 isClosed()가 다시
 * true로 평가돼도 재발송하지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostingClosedAlarmScheduler {

    private final PostingRepository postingRepository;
    private final AlramService alramService;
    private final AlramDispatcher alramDispatcher;
    private final AlramFailureRecorder alramFailureRecorder;

    @Scheduled(cron = "0 0 0 * * *")
    public void dispatchClosedAlrams() {
        List<Posting> candidates = postingRepository.findClosedAlramCandidates();
        int sent = 0;
        int skipped = 0;

        for (Posting posting : candidates) {
            if (!posting.isClosed()) {
                skipped++;
                continue;
            }
            try {
                AlramDispatch dispatch = alramService.prepareClosedAlrams(posting.getId());   // 트랜잭션
                if (dispatch.isEmpty()) {
                    continue;
                }
                alramDispatcher.dispatch(posting.getId(), null, null, dispatch);   // 트랜잭션 밖 + 재시도
                sent++;
            } catch (Exception e) {
                log.error("마감 알람 준비 실패 - postingId={}", posting.getId(), e);
                alramFailureRecorder.record(posting.getId(), null, null, e);
            }
        }

        log.info("공고 마감 알람 스캔 완료 - 후보 {}건, 발송 {}건, 아직 마감 아님 {}건",
                candidates.size(), sent, skipped);
    }
}
