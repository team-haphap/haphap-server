package org.sopt.haphap.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.user.dto.WithdrawRequest;
import org.sopt.haphap.domain.user.service.withdrawal.WithdrawalUnlinkAsyncRunner;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberWithdrawalService {

    private final WithdrawalTransactionService withdrawalTransactionService;
    private final WithdrawalUnlinkAsyncRunner withdrawalUnlinkAsyncRunner;

    public void withdraw(Long userId, String accessToken, WithdrawRequest request) {
        boolean started = withdrawalTransactionService.start(userId, accessToken, request);
        if (started) {
            withdrawalUnlinkAsyncRunner.run(userId);   // 응답과 무관하게 비동기 진행
        }
    }
}