package org.sopt.haphap.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.user.dto.WithdrawRequest;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.user.entity.WithdrawalReasonLog;
import org.sopt.haphap.domain.user.repository.UserRepository;
import org.sopt.haphap.domain.user.repository.WithdrawalReasonLogRepository;
import org.sopt.haphap.domain.user.service.withdrawal.WithdrawalCleaner;
import org.sopt.haphap.global.code.GlobalErrorCode;
import org.sopt.haphap.global.exception.CustomException;
import org.sopt.haphap.global.jwt.JwtProperties;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.sopt.haphap.global.jwt.TokenService;
import org.sopt.haphap.global.jwt.Role;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WithdrawalTransactionService {

    private static final int MAX_UNLINK_RETRY = 5;

    private final UserRepository userRepository;
    private final List<WithdrawalCleaner> cleaners;
    private final WithdrawalReasonLogRepository withdrawalReasonLogRepository;

    private final TokenService tokenService;
    private final JwtProperties jwtProperties;

    /**  잠금 → 상태 확인 → 데이터 삭제 → 개인정보 파기 */
    @Transactional
    public boolean start(Long userId, String accessToken, WithdrawRequest request) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.USER_NOT_FOUND));
        if (!user.isActive()) {
            return false;   // 동시에 들어온 두 번째 요청: 최초 요청이 이미 처리 → 에러 대신 성공 응답
        }
        cleaners.forEach(cleaner -> cleaner.clean(userId));
        user.startWithdrawal();
        withdrawalReasonLogRepository.save(
                WithdrawalReasonLog.of(request.reason(), request.normalizedEtcReason()));

        // 커밋 전에 폐기 → Redis 실패 시 전체 롤백(서버 오류 시 현재 상태 유지),
        // 성공 시 "탈퇴됐는데 토큰이 살아 있는" 상태가 생기지 않음
        tokenService.blacklistAccessToken(accessToken);
        tokenService.deleteRefreshToken(userId, Role.USER);
        tokenService.markWithdrawn(userId, jwtProperties.accessTokenExpiry());
        return true;
    }

    @Transactional
    public void complete(Long userId) {
        userRepository.findByIdForUpdate(userId).ifPresent(User::completeWithdrawal);
    }

    @Transactional
    public void recordFailure(Long userId) {
        userRepository.findByIdForUpdate(userId).ifPresent(user -> {
            if (user.recordUnlinkFailure(MAX_UNLINK_RETRY)) {
                log.error("[탈퇴 연동해제 최종 실패] userId={}, provider={} - 수동 처리 필요",
                        user.getId(), user.getProvider());
                // TODO: 슬랙/디스코드 알림
            }
        });
    }

    //관리자 수동 복구용
    @Transactional
    public void resetUnlinkRetry(Long userId) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.USER_NOT_FOUND));
        user.resetUnlinkRetry();
        log.info("[탈퇴] 연동 해제 재시도 상태로 복구 userId={}", userId);
    }
}