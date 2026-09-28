package org.sopt.haphap.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.user.dto.WithdrawRequest;
import org.sopt.haphap.domain.user.service.withdrawal.WithdrawalUnlinkProcessor;
import org.sopt.haphap.global.jwt.Role;
import org.sopt.haphap.global.jwt.TokenService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberWithdrawalService {

    private final WithdrawalTransactionService withdrawalTransactionService;
    private final WithdrawalUnlinkProcessor withdrawalUnlinkProcessor;
    private final TokenService tokenService;

    public void withdraw(Long userId, String accessToken, WithdrawRequest request) {
        withdrawalTransactionService.start(userId, request);   // 커밋되면 개인정보는 이미 파기됨
        invalidateTokensQuietly(userId, accessToken);          // 실패해도 reissue에서 막힘(4번)
        processUnlinkQuietly(userId);                          // 실패해도 스케줄러가 재시도
    }

    private void invalidateTokensQuietly(Long userId, String accessToken) {
        try {
            tokenService.blacklistAccessToken(accessToken);
            tokenService.deleteRefreshToken(userId, Role.USER);
        } catch (Exception e) {
            log.warn("탈퇴 토큰 폐기 실패 userId={}", userId, e);
        }
    }

    // start() 커밋 이후 단계의 예외가 클라한테 500으로 넘어가지 않도록 -> 응답은 성공으로, 남은 작업은 WithdrawalUnlinkRetrySceduler가 보완
    private void processUnlinkQuietly(Long userId) {
        try {
            withdrawalUnlinkProcessor.process(userId);
        } catch (Exception e) {
            log.warn("[탈퇴] 즉시 연동 해제 단계 실패 - 스케줄러가 재시도 userId={}", userId, e);
        }
    }
}