package org.sopt.haphap.domain.registration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.registration.code.RegistrationErrorCode;
import org.sopt.haphap.domain.registration.domain.Registration;
import org.sopt.haphap.domain.registration.event.RegistrationApprovedEvent;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.sopt.haphap.domain.verification.entity.VerificationImage;
import org.sopt.haphap.domain.verification.repository.VerificationImageRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.sopt.haphap.global.s3.S3Uploader;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationVerificationDecisionService {

    private final RegistrationRepository registrationRepository;
    private final VerificationImageRepository verificationImageRepository;
    private final S3Uploader s3Uploader;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void approve(Long registrationId) {
        Registration registration = registrationRepository.findByIdForUpdate(registrationId)
                .orElseThrow(() -> new CustomException(RegistrationErrorCode.REGISTRATION_NOT_FOUND));

        registration.approve();

        eventPublisher.publishEvent(new RegistrationApprovedEvent(
                registration.getPosting().getId(), registration.getStage().getId()));
    }

    @Transactional
    public void reject(Long registrationId) {
        Registration registration = registrationRepository.findByIdForUpdate(registrationId)
                .orElseThrow(() -> new CustomException(RegistrationErrorCode.REGISTRATION_NOT_FOUND));

        registration.reject();

        cleanupImages(registrationId);
    }

    private void cleanupImages(Long registrationId) {
        List<VerificationImage> images = verificationImageRepository.findByRegistrationId(registrationId);
        images.forEach(image -> {
            try {
                s3Uploader.deletePrivate(image.getS3Key());
            } catch (Exception e) {
                log.warn("반려 이미지 삭제 실패(고아 정리 배치에서 재처리됨) key={}", image.getS3Key(), e);
            }
        });
        verificationImageRepository.deleteAll(images);
    }
}