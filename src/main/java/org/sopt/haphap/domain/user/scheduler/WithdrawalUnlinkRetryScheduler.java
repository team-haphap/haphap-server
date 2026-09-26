package org.sopt.haphap.domain.user.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.user.entity.WithdrawalStatus;
import org.sopt.haphap.domain.user.repository.UserRepository;
import org.sopt.haphap.domain.user.service.withdrawal.WithdrawalUnlinkProcessor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

// 외부 연동 해제에 실패한 탈퇴 건을 주기적으로 재시도한다. 연동 해제는 여러 번 호출돼도 결과가 같아서(카카오 -101 성공 처리, 애플 revoke),
// 서버가 여러 대여도 중복 실행이 문제되지 않는다. (현재는 단일 인스턴스 배포)

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawalUnlinkRetryScheduler {

    private static final Duration GRACE = Duration.ofMinutes(5);   // 방금 API가 시도 중인 건은 건너뜀
    private static final int BATCH_SIZE = 100;

    private final UserRepository userRepository;
    private final WithdrawalUnlinkProcessor processor;

    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void retry() {
        List<Long> ids = userRepository.findIdsForUnlinkRetry(
                WithdrawalStatus.PENDING_UNLINK,
                LocalDateTime.now().minus(GRACE),
                PageRequest.of(0, BATCH_SIZE));
        if (ids.isEmpty()) {
            return;
        }
        log.info("탈퇴 연동 해제 재시도 대상 {}건", ids.size());
        ids.forEach(processor::process);
    }
}