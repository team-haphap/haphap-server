package org.sopt.haphap.domain.verification.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.user.service.withdrawal.WithdrawalCleaner;
import org.sopt.haphap.domain.verification.repository.VerificationImageRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VerificationWithdrawalCleaner implements WithdrawalCleaner {

    private final VerificationImageRepository verificationImageRepository;

    // 연결을 끊어 고아로 만들면 OrphanVerificationImageCleaner가 S3 + 행을 재시도 포함해 삭제.
    // 업로드 24시간 이내 이미지도 최대 약 25시간 안에 삭제 → 5일 기준 충족

    @Override
    public void clean(Long userId) {
        verificationImageRepository.detachAllByUserId(userId);
    }
}