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
        try {
            socialUnlinker.unlink(user);                        // 외부 API (트랜잭션 밖)
            withdrawalTransactionService.complete(userId);     // 성공 → WITHDRAWN
        } catch (Exception e) {
            log.warn("탈퇴 연동 해제 실패(재시도 예정) userId={}", userId, e);
            withdrawalTransactionService.recordFailure(userId); // 실패 → 횟수 +1, 5회면 UNLINK_FAILED
        }
    }
}