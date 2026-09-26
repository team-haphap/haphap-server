package org.sopt.haphap.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.user.dto.WithdrawRequest;
import org.sopt.haphap.domain.user.service.withdrawal.SocialUnlinker;
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
        withdrawalUnlinkProcessor.process(userId);             // 실패하면 스케줄러가 재시도
    }

    private void invalidateTokensQuietly(Long userId, String accessToken) {
        try {
            tokenService.blacklistAccessToken(accessToken);
            tokenService.deleteRefreshToken(userId, Role.USER);
        } catch (Exception e) {
            log.warn("탈퇴 토큰 폐기 실패 userId={}", userId, e);
        }
    }
}