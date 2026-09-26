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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WithdrawalTransactionService {

    private static final int MAX_UNLINK_RETRY = 5;

    private final UserRepository userRepository;
    private final List<WithdrawalCleaner> cleaners;
    private final WithdrawalReasonLogRepository withdrawalReasonLogRepository;

    /**  잠금 → 상태 확인 → 데이터 삭제 → 개인정보 파기 */
    @Transactional
    public void start(Long userId, WithdrawRequest request) {
        User user = userRepository.findByIdForUpdate(userId)      // 같은 유저의 동시 요청은 여기서 줄을 섬
                .orElseThrow(() -> new CustomException(GlobalErrorCode.USER_NOT_FOUND));
        if (!user.isActive()) {
            throw new CustomException(GlobalErrorCode.USER_NOT_FOUND);   // 두 번째 요청은 여기서 막힘
        }
        cleaners.forEach(cleaner -> cleaner.clean(userId));
        user.startWithdrawal();
        withdrawalReasonLogRepository.save(
                WithdrawalReasonLog.of(request.reason(), request.normalizedEtcReason()));
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
}