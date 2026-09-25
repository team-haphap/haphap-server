package org.sopt.haphap.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.user.dto.WithdrawRequest;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.user.service.withdrawal.SocialUnlinker;
import org.sopt.haphap.global.code.GlobalErrorCode;
import org.sopt.haphap.global.exception.CustomException;
import org.sopt.haphap.global.jwt.Role;
import org.sopt.haphap.global.jwt.TokenService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberWithdrawalService {

    private final UserService userService;
    private final SocialUnlinker socialUnlinker;
    private final WithdrawalTransactionService withdrawalTransactionService;
    private final TokenService tokenService;

    public void withdraw(Long userId, String accessToken, WithdrawRequest request) {
        User user = userService.findById(userId);

        if (user.isWithdrawn()){
            throw new CustomException(GlobalErrorCode.USER_NOT_FOUND);
        }

        socialUnlinker.unlink(user);                                  // 트랜잭션 밖 (외부 API)
        withdrawalTransactionService.withdraw(userId, request);       // DB 트랜잭션
        tokenService.blacklistAccessToken(accessToken);               // 커밋 후 Redis
        tokenService.deleteRefreshToken(userId, Role.USER);
    }
}