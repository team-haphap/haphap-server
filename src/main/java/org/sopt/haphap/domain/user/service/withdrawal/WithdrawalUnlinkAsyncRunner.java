package org.sopt.haphap.domain.user.service.withdrawal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawalUnlinkAsyncRunner {

    private final WithdrawalUnlinkProcessor withdrawalUnlinkProcessor;

    // 탈퇴 트랜잭션 커밋 이후, 응답과 별개로 실행. 실패해도 스케줄러가 재시도
    @Async
    public void run(Long userId) {
        try {
            withdrawalUnlinkProcessor.process(userId);
        } catch (Exception e) {
            log.warn("[탈퇴] 연동 해제 단계 실패 - 스케줄러가 재시도 userId={}", userId, e);
        }
    }
}