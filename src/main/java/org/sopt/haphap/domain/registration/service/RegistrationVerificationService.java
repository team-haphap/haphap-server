package org.sopt.haphap.domain.registration.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.registration.code.RegistrationErrorCode;
import org.sopt.haphap.domain.registration.domain.Registration;
import org.sopt.haphap.domain.registration.dto.response.VerificationResubmitResponse;
import org.sopt.haphap.domain.registration.event.VerificationResubmittedEvent;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.sopt.haphap.domain.verification.entity.VerificationImage;
import org.sopt.haphap.domain.verification.repository.VerificationImageRepository;
import org.sopt.haphap.domain.verification.service.VerificationImageAttacher;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistrationVerificationService {

    private final RegistrationRepository registrationRepository;
    private final VerificationImageRepository verificationImageRepository;
    private final VerificationImageAttacher verificationImageAttacher;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public VerificationResubmitResponse resubmit(
            Long userId,
            Long registrationId,
            List<Long> imageIds
    ) {
        Registration registration = registrationRepository
                .findByIdAndUserIdForUpdate(registrationId, userId)
                .orElseThrow(() -> new CustomException(RegistrationErrorCode.REGISTRATION_NOT_FOUND));

        registration.resubmitVerification();

        verificationImageRepository
                .findAttachedByRegistrationIdOrdered(registrationId)
                .forEach(VerificationImage::detach);

        verificationImageAttacher.attach(registration, userId, imageIds);

        eventPublisher.publishEvent(
                new VerificationResubmittedEvent(
                        registration.getId(),
                        registration.getVerificationRound()
                )
        );

        return VerificationResubmitResponse.from(registration);
    }
}