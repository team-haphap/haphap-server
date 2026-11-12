package org.sopt.haphap.domain.registration.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.domain.CompanyImage;
import org.sopt.haphap.domain.posting.domain.CompanyImageType;
import org.sopt.haphap.domain.posting.repository.CompanyImageRepository;
import org.sopt.haphap.domain.registration.domain.Registration;
import org.sopt.haphap.domain.registration.domain.RegistrationVerificationStatus;
import org.sopt.haphap.domain.registration.dto.response.VerificationSummaryResponse;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.sopt.haphap.domain.verification.entity.VerificationImage;
import org.sopt.haphap.domain.verification.repository.VerificationImageRepository;
import org.sopt.haphap.global.s3.S3PresignedUrlGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VerificationQueryService {

    private static final List<RegistrationVerificationStatus> TARGETS = List.of(
            RegistrationVerificationStatus.PENDING,
            RegistrationVerificationStatus.APPROVED,
            RegistrationVerificationStatus.REJECTED
    );

    private final RegistrationRepository registrationRepository;
    private final CompanyImageRepository companyImageRepository;
    private final VerificationImageRepository verificationImageRepository;
    private final S3PresignedUrlGenerator presignedUrlGenerator;

    public VerificationSummaryResponse getSummary(Long userId) {
        List<Registration> registrations =
                registrationRepository.findVerificationHistory(userId, TARGETS);

        if (registrations.isEmpty()) {
            return VerificationSummaryResponse.from(
                    registrations,
                    Map.of(),
                    Map.of()
            );
        }

        List<Long> companyIds = registrations.stream()
                .map(registration -> registration.getPosting().getCompany().getId())
                .distinct()
                .toList();

        Map<Long, String> squareLogoUrlsByCompanyId = companyImageRepository
                .findByCompanyIdInAndType(companyIds, CompanyImageType.CALENDAR_LOGO)
                .stream()
                .collect(Collectors.toMap(
                        image -> image.getCompany().getId(),
                        CompanyImage::getImageUrl
                ));

        List<Long> rejectedRegistrationIds = registrations.stream()
                .filter(registration ->
                        registration.getVerificationStatus() == RegistrationVerificationStatus.REJECTED)
                .map(Registration::getId)
                .toList();

        Map<Long, List<String>> submittedImageUrlsByRegistrationId =
                rejectedRegistrationIds.isEmpty()
                        ? Map.of()
                        : verificationImageRepository
                          .findAttachedByRegistrationIds(rejectedRegistrationIds)
                          .stream()
                          .collect(Collectors.groupingBy(
                                  image -> image.getRegistration().getId(),
                                  LinkedHashMap::new,
                                  Collectors.mapping(
                                          image -> presignedUrlGenerator.generateGetUrl(image.getS3Key()),
                                          Collectors.toList()
                                  )
                          ));

        return VerificationSummaryResponse.from(
                registrations,
                squareLogoUrlsByCompanyId,
                submittedImageUrlsByRegistrationId
        );
    }
}