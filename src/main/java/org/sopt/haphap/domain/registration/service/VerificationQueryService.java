package org.sopt.haphap.domain.registration.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.registration.domain.RegistrationVerificationStatus;
import org.sopt.haphap.domain.registration.dto.response.VerificationSummaryResponse;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VerificationQueryService {

    private static final List<RegistrationVerificationStatus> TARGETS = List.of(
            RegistrationVerificationStatus.PENDING,
            RegistrationVerificationStatus.APPROVED,
            RegistrationVerificationStatus.REJECTED);

    private final RegistrationRepository registrationRepository;

    public VerificationSummaryResponse getSummary(Long userId) {
        return VerificationSummaryResponse.from(
                registrationRepository.findVerificationHistory(userId, TARGETS));
    }
}