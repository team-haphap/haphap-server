package org.sopt.haphap.domain.registration.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.sopt.haphap.domain.user.service.withdrawal.WithdrawalCleaner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegistrationWithdrawalCleaner implements WithdrawalCleaner {

    private final RegistrationRepository registrationRepository;

    // 전형 기록은 전체 합불 집계(StageResultCountReconciler)의 원본이라 행은 유지 -> 집계 보존.
    // 개인화 필드(연락 수단/시각)는 파기하고, 검토 대기 인증은 반려로 닫아 탈퇴자 승인을 막는다.

    @Override
    public void clean(Long userId) {
        registrationRepository.anonymizeAllByUserId(userId);
    }
}