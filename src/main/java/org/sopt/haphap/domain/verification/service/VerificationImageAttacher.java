package org.sopt.haphap.domain.verification.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.registration.domain.Registration;
import org.sopt.haphap.domain.verification.code.VerificationErrorCode;
import org.sopt.haphap.domain.verification.code.VerificationImagePolicy;
import org.sopt.haphap.domain.verification.entity.VerificationImage;
import org.sopt.haphap.domain.verification.repository.VerificationImageRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Component;

// 등록 최초 생성과 반려 재제출이 함께 쓰는 이미지 검증 + 락 + 연결 로직
@Component
@RequiredArgsConstructor
public class VerificationImageAttacher {

    private final VerificationImageRepository verificationImageRepository;

    public void attach(Registration registration, Long userId, List<Long> imageIds) {
        List<Long> ids = (imageIds == null ? List.<Long>of() : imageIds).stream().distinct().toList();
        validateCount(ids);

        List<VerificationImage> images = verificationImageRepository.findAllByIdInForUpdate(ids);
        if (images.size() != ids.size()) {
            throw new CustomException(VerificationErrorCode.IMAGE_NOT_FOUND);
        }
        for (VerificationImage image : images) {
            if (!image.isOwnedBy(userId)) {
                throw new CustomException(VerificationErrorCode.IMAGE_NOT_OWNED);
            }
        }
        for (int i = 0; i < images.size(); i++) {
            images.get(i).attachTo(registration, i);
        }
    }

    private void validateCount(List<Long> ids) {
        if (ids.size() < VerificationImagePolicy.MIN_IMAGE_COUNT
                || ids.size() > VerificationImagePolicy.MAX_IMAGE_COUNT) {
            throw new CustomException(VerificationErrorCode.IMAGE_COUNT_INVALID);
        }
    }
}