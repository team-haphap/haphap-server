package org.sopt.haphap.domain.registration.dto.response;

import org.sopt.haphap.domain.registration.domain.Registration;
import org.sopt.haphap.domain.registration.domain.RegistrationVerificationStatus;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record VerificationSummaryResponse(
        int pendingCount,
        int approvedCount,
        int rejectedCount,
        List<VerificationItemResponse> items
) {
    public static VerificationSummaryResponse from(List<Registration> registrations) {
        Map<RegistrationVerificationStatus, Long> counts = registrations.stream()
                .collect(Collectors.groupingBy(Registration::getVerificationStatus, Collectors.counting()));
        return new VerificationSummaryResponse(
                counts.getOrDefault(RegistrationVerificationStatus.PENDING, 0L).intValue(),
                counts.getOrDefault(RegistrationVerificationStatus.APPROVED, 0L).intValue(),
                counts.getOrDefault(RegistrationVerificationStatus.REJECTED, 0L).intValue(),
                registrations.stream().map(VerificationItemResponse::from).toList());
    }
}