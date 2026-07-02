package org.sopt.haphap.domain.registration.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.registration.code.RegistrationSuccessCode;
import org.sopt.haphap.domain.registration.code.RegistrationErrorCode;
import org.sopt.haphap.domain.registration.domain.Registration;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.user.repository.UserRepository;
import org.sopt.haphap.global.code.SuccessResultCode;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class RegistrationCheckService {

    private final RegistrationRepository registrationRepository;
    private final RegistrationTargetValidator registrationTargetValidator;

    @Transactional(readOnly = true)
    public SuccessResultCode check(Long userId, Long postingId, Long stageId) {

        registrationTargetValidator.validate(userId, postingId, stageId);

        return registrationRepository
                .findByUserIdAndPostingIdAndStageId(userId, postingId, stageId)
                .map(this::judgeExisting)
                .orElse(RegistrationSuccessCode.NEW_REGISTRATION);  // 등록 이력 없음 → 신규
    }

    private SuccessResultCode judgeExisting(Registration existing) {
        // 기존이 대기(PENDING) 상태 → 변경 가능하니 확인 토글 유도
        if (existing.isPending()) {
            return RegistrationSuccessCode.REGISTRATION_CONFIRM_REQUIRED;
        }
        // 기존이 이미 확정(PASS/FAIL) → 다시 등록 불가
        throw new CustomException(RegistrationErrorCode.DUPLICATE_REGISTRATION);
    }
}