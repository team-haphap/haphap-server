package org.sopt.haphap.domain.registration.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.registration.code.RegistrationErrorCode;
import org.sopt.haphap.domain.registration.domain.Registration;
import org.sopt.haphap.domain.registration.dto.response.VerificationResubmitResponse;
import org.sopt.haphap.domain.registration.event.VerificationResubmittedEvent;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.sopt.haphap.domain.verification.service.VerificationImageAttacher;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrationVerificationService {

    private final RegistrationRepository registrationRepository;
    private final VerificationImageAttacher verificationImageAttacher;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public VerificationResubmitResponse resubmit(Long userId, Long registrationId, List<Long> imageIds) {
        // 1) 본인 등록 행에 락. 승인/반려 처리, 다른 재제출 요청과 직렬화
        Registration registration = registrationRepository.findByIdAndUserIdForUpdate(registrationId, userId)
                .orElseThrow(() -> new CustomException(RegistrationErrorCode.REGISTRATION_NOT_FOUND));

        // 2) REJECTED에서 PENDING로 전이.
        // 이미 PENDING/APPROVED면 409 (더블클릭/중복 요청 방어)
        registration.resubmitVerification();

        // 3) 이미지 검증/락/연결.
        // 실패하면 전이도 함께 롤백
        verificationImageAttacher.attach(registration, userId, imageIds);

        // 4) 운영진 Discord 알림
        eventPublisher.publishEvent(
                new VerificationResubmittedEvent(registration.getId(), registration.getVerificationRound()));

        return VerificationResubmitResponse.from(registration);
    }
}