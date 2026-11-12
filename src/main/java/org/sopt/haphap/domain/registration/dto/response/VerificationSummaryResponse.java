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
    public static VerificationSummaryResponse from(
            List<Registration> registrations,
            Map<Long, String> squareLogoUrlsByCompanyId,
            Map<Long, List<String>> submittedImageUrlsByRegistrationId
    ) {
        Map<RegistrationVerificationStatus, Long> counts = registrations.stream()
                .collect(Collectors.groupingBy(
                        Registration::getVerificationStatus,
                        Collectors.counting()
                ));

        List<VerificationItemResponse> items = registrations.stream()
                .map(registration -> {
                    Long companyId = registration.getPosting().getCompany().getId();
                    Long registrationId = registration.getId();

                    List<String> imageUrls =
                            registration.getVerificationStatus()
                                    == RegistrationVerificationStatus.REJECTED
                                    ? submittedImageUrlsByRegistrationId.getOrDefault(
                                    registrationId,
                                    List.of()
                            )
                                    : List.of();

                    return VerificationItemResponse.from(
                            registration,
                            squareLogoUrlsByCompanyId.get(companyId),
                            imageUrls
                    );
                })
                .toList();

        return new VerificationSummaryResponse(
                counts.getOrDefault(RegistrationVerificationStatus.PENDING, 0L).intValue(),
                counts.getOrDefault(RegistrationVerificationStatus.APPROVED, 0L).intValue(),
                counts.getOrDefault(RegistrationVerificationStatus.REJECTED, 0L).intValue(),
                items
        );
    }
}