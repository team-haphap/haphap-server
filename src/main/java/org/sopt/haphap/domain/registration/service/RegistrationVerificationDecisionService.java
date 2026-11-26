package org.sopt.haphap.domain.registration.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.registration.code.RegistrationErrorCode;
import org.sopt.haphap.domain.registration.domain.Registration;
import org.sopt.haphap.domain.registration.event.RegistrationApprovedEvent;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrationVerificationDecisionService {

    private final RegistrationRepository registrationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void approve(Long registrationId, int round) {
        Registration registration = registrationRepository.findByIdForUpdate(registrationId)
                .orElseThrow(() -> new CustomException(RegistrationErrorCode.REGISTRATION_NOT_FOUND));
        validateRound(registration, round);
        registration.approve();

        eventPublisher.publishEvent(new RegistrationApprovedEvent(
                registration.getPosting().getId(),
                registration.getStage().getId(),
                registration.getUser().getId(),
                registration.getId()
        ));
    }

    @Transactional
    public void reject(Long registrationId, int round, String reason) {
        Registration registration = registrationRepository.findByIdForUpdate(registrationId)
                .orElseThrow(() -> new CustomException(RegistrationErrorCode.REGISTRATION_NOT_FOUND));
        validateRound(registration, round);

        registration.reject(reason);
    }

    private void validateRound(Registration registration, int round) {
        if (registration.getVerificationRound() != round) {
            throw new CustomException(RegistrationErrorCode.VERIFICATION_LINK_EXPIRED);
        }
    }
}