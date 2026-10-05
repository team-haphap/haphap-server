package org.sopt.haphap.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.user.dto.WithdrawRequest;
import org.sopt.haphap.domain.user.service.withdrawal.WithdrawalUnlinkProcessor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberWithdrawalService {

    private final WithdrawalTransactionService withdrawalTransactionService;
    private final WithdrawalUnlinkProcessor withdrawalUnlinkProcessor;

    public void withdraw(Long userId, String accessToken, WithdrawRequest request) {
        boolean started = withdrawalTransactionService.start(userId, accessToken, request);
        if (started) {
            processUnlinkQuietly(userId);   // 실패해도 스케줄러가 재시도
        }
    }

    private void processUnlinkQuietly(Long userId) {
        try {
            withdrawalUnlinkProcessor.process(userId);
        } catch (Exception e) {
            log.warn("[탈퇴] 즉시 연동 해제 단계 실패 - 스케줄러가 재시도 userId={}", userId, e);
        }
    }
}