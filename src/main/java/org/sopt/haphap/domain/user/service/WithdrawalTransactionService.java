package org.sopt.haphap.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.user.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class WithdrawalTransactionService {

    private static final int MAX_RETRY = 5;
    private final UserRepository userRepository;

    @Transactional
    public void finalizeWithdrawal(Long userId) {
        userRepository.findById(userId).ifPresent(User::withdraw);
    }

    @Transactional
    public void handleRetryFailure(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.incrementWithdrawalRetryCount();
            if (user.getWithdrawalRetryCount() >= MAX_RETRY) {
                alertOps(user);
            }
        });
    }

    private void alertOps(User user) {
        log.error("[탈퇴 연동해제 실패] userId={}, provider={} - {}회 재시도 후에도 실패, 수동 확인 필요",
                user.getId(), user.getProvider(), user.getWithdrawalRetryCount());
        // TODO: 슬랙이나 디스코드로 알림 붙일 때 여기서 호출
    }
}