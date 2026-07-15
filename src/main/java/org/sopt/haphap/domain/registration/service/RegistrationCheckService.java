package org.sopt.haphap.domain.registration.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.registration.code.RegistrationSuccessCode;
import org.sopt.haphap.domain.registration.code.RegistrationErrorCode;
import org.sopt.haphap.domain.registration.domain.Registration;
import org.sopt.haphap.domain.registration.domain.RegistrationResult;
import org.sopt.haphap.domain.registration.dto.request.RegistrationCheckRequest;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
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
    public SuccessResultCode check(Long userId, Long postingId, Long stageId, RegistrationCheckRequest request) {

        registrationTargetValidator.validate(userId, postingId, stageId);

        return registrationRepository
                .findByUserIdAndPostingIdAndStageId(userId, postingId, stageId)
                .map(existing -> judgeExisting(existing, request.result()))
                .orElse(RegistrationSuccessCode.NEW_REGISTRATION);  // 등록 이력 없음 → 신규
    }

    private SuccessResultCode judgeExisting(Registration existing, RegistrationResult newResult) {

        // 기존이 대기(PENDING) 상태
        if (existing.isPending()) {
            // 새로 들어온 것도 대기 → 중복
            if (newResult == RegistrationResult.PENDING) {
                throw new CustomException(RegistrationErrorCode.DUPLICATE_REGISTRATION);
            }
            // 새로 들어온 것이 대기가 아님 → 확인 토글 유도
            return RegistrationSuccessCode.REGISTRATION_CONFIRM_REQUIRED;
        }
        // 기존이 확정(PASS/FAIL) → 항상 중복
        throw new CustomException(RegistrationErrorCode.DUPLICATE_REGISTRATION);
    }

}