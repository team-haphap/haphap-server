package org.sopt.haphap.domain.user.service.withdrawal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.user.entity.WithdrawalStatus;
import org.sopt.haphap.domain.user.repository.UserRepository;
import org.sopt.haphap.domain.user.service.WithdrawalTransactionService;
import org.springframework.stereotype.Component;

// 탈퇴 처리 중인 회원의 외부 연동 해제를 1회 시도한다. 탈퇴 API와 재시도 스케줄러가 함께 사용한다.

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawalUnlinkProcessor {

    private final UserRepository userRepository;
    private final SocialUnlinker socialUnlinker;
    private final WithdrawalTransactionService withdrawalTransactionService;

    public void process(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getWithdrawalStatus() != WithdrawalStatus.PENDING_UNLINK) {
            return;   // 이미 완료됐거나 종료 상태면 할 일 없음
        }

        // 외부 연동 해제 — 실패만 "연동 해제 실패"로 카운트
        try {
            socialUnlinker.unlink(user);   //외부 API
        } catch (Exception e) {
            log.warn("[탈퇴] 외부 연동 해제 실패(재시도 예정) userId={}, provider={}",
                    userId, user.getProvider(), e);
            withdrawalTransactionService.recordFailure(userId); // 횟수 +1, 5회면 UNLINK_FAILED
            return;
        }

        // 완료 처리 - 해제는 성공했으므로 실패 횟수로 세지 않고
        // 상태가 pending_unlink로 남아 스케줄러가 다시 시도하고, 연동 해제는 멱등이라 결과는 같다
        try {
            withdrawalTransactionService.complete(userId);
        } catch (Exception e) {
            log.error("[탈퇴] 연동 해제 성공, 완료 처리(DB) 실패 - 스케줄러 재시도 예정 userId={}", userId, e);
        }
    }
}