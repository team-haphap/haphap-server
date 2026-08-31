package org.sopt.haphap.domain.user.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.user.entity.WithdrawalStatus;
import org.sopt.haphap.domain.user.repository.UserRepository;
import org.sopt.haphap.domain.user.service.WithdrawalTransactionService;
import org.sopt.haphap.global.client.AppleOAuthClient;
import org.sopt.haphap.global.client.KakaoOAuthClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import org.sopt.haphap.domain.user.entity.User;

@Component
@RequiredArgsConstructor
@Slf4j
public class WithdrawalRetryScheduler {

    private final UserRepository userRepository;
    private final KakaoOAuthClient kakaoOAuthClient;
    private final AppleOAuthClient appleOAuthClient;
    private final WithdrawalTransactionService withdrawalTransactionService; // 다른 빈이라 프록시 정상 작동

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void retryPendingUnlinks() {
        List<User> pending = userRepository.findTop50ByWithdrawalStatusOrderByWithdrawalRequestedAtAsc(WithdrawalStatus.PENDING_UNLINK);
        for (User user : pending) {
            try {
                unlinkExternal(user);
                withdrawalTransactionService.finalizeWithdrawal(user.getId());
            } catch (Exception e) {
                log.warn("연동 해제 재시도 실패 userId={}, provider={}", user.getId(), user.getProvider(), e);
                withdrawalTransactionService.handleRetryFailure(user.getId());
            }
        }
    }

    private void unlinkExternal(User user) {
        switch (user.getProvider()) {
            case KAKAO -> kakaoOAuthClient.unlink(user.getProviderId());
            case APPLE -> {
                if (user.getAppleRefreshToken() == null) {
                    throw new IllegalStateException("appleRefreshToken 없음 - 자동 재시도 불가, 수동 확인 필요");
                }
                appleOAuthClient.revoke(user.getAppleRefreshToken());
            }
            case LOCAL -> { } // 외부 연동 없음, 통과
        }
    }
}