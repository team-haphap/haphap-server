package org.sopt.haphap.domain.verification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.user.service.UserService;
import org.sopt.haphap.domain.verification.code.VerificationErrorCode;
import org.sopt.haphap.domain.verification.dto.response.VerificationImageUploadResponse;
import org.sopt.haphap.domain.verification.entity.VerificationImage;
import org.sopt.haphap.domain.verification.repository.VerificationImageRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.sopt.haphap.global.s3.S3Uploader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationImageService {
    private static final int MIN_IMAGE_COUNT = 1;
    private static final int MAX_IMAGE_COUNT = 3;
    private static final String DIR_NAME = "pass-verifications";

    private final S3Uploader s3Uploader;
    private final VerificationImageRepository verificationImageRepository;
    private final UserService userService;

    public VerificationImageUploadResponse upload(Long userId, List<MultipartFile> files) {
        validateCount(files);
        User user = userService.findById(userId);

        List<String> uploadedKeys = new ArrayList<>();
        try {
            // S3 업로드 (외부 I/O — 트랜잭션 밖)
            for (MultipartFile file : files) {
                uploadedKeys.add(s3Uploader.uploadPrivate(file, DIR_NAME + "/" + userId));
            }
            // DB 저장 (saveAll은 자체 트랜잭션으로 짧게)
            List<VerificationImage> saved = verificationImageRepository.saveAll(
                    uploadedKeys.stream().map(key -> VerificationImage.uploadedBy(user, key)).toList());
            return VerificationImageUploadResponse.from(saved);

        } catch (RuntimeException e) {
            // 보상: 중간에 실패하면 이미 올라간 파일을 지우기
            uploadedKeys.forEach(this::deleteQuietly);
            throw e;
        }
    }

    private void validateCount(List<MultipartFile> files) {
        int count = (files == null) ? 0 : files.size();
        if (count < MIN_IMAGE_COUNT || count > MAX_IMAGE_COUNT) {
            throw new CustomException(VerificationErrorCode.IMAGE_COUNT_INVALID);
        }
    }

    private void deleteQuietly(String key) {
        try {
            s3Uploader.deletePrivate(key);
        } catch (Exception e) {
            log.warn("업로드 보상 삭제 실패(고아 정리 배치에서 재처리됨) key={}", key, e);
        }
    }
}
