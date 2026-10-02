package org.sopt.haphap.domain.verification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.user.service.UserService;
import org.sopt.haphap.domain.verification.code.VerificationErrorCode;
import org.sopt.haphap.domain.verification.code.VerificationImagePolicy;
import org.sopt.haphap.domain.verification.dto.response.VerificationImageUploadResponse;
import org.sopt.haphap.domain.verification.entity.VerificationImage;
import org.sopt.haphap.domain.verification.repository.VerificationImageRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.sopt.haphap.global.s3.S3Uploader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationImageService {
    private static final String DIR_NAME = "pass-verifications";

    private final S3Uploader s3Uploader;
    private final VerificationImageRepository verificationImageRepository;
    private final UserService userService;

    public VerificationImageUploadResponse upload(Long userId, List<MultipartFile> files) {
        validateCount(files);
        validateFiles(files);

        User user = userService.findById(userId);
        String dirName = DIR_NAME + "/" + userId;

        List<VerificationImage> reserved = verificationImageRepository.saveAll(
                files.stream()
                        .map(file -> VerificationImage.uploadedBy(user, s3Uploader.newPrivateKey(dirName)))
                        .toList());

        try {
            for (int i = 0; i < files.size(); i++) {
                s3Uploader.uploadPrivate(files.get(i), reserved.get(i).getS3Key());
            }
            return VerificationImageUploadResponse.from(reserved);

        } catch (RuntimeException e) {
            List<VerificationImage> successfullyDeletedImages = new ArrayList<>();

            for (VerificationImage image : reserved) {
                try {
                    s3Uploader.deletePrivate(image.getS3Key());
                    successfullyDeletedImages.add(image);
                } catch (Exception ex) {
                    log.warn("업로드 보상 삭제 실패(고아 정리 배치에서 재처리됨) key={}", image.getS3Key(), ex);
                }
            }

            if (!successfullyDeletedImages.isEmpty()) {
                deleteRowsQuietly(successfullyDeletedImages);
            }

            throw e;
        }
    }

    private void deleteRowsQuietly(List<VerificationImage> images) {
        try {
            verificationImageRepository.deleteAll(images);
        } catch (Exception e) {
            log.warn("업로드 보상 행 삭제 실패(정리 배치에서 재처리됨) ids={}",
                    images.stream().map(VerificationImage::getId).toList(), e);
        }
    }

    private void validateCount(List<MultipartFile> files) {
        int size = files != null ? files.size() : 0;
        if (size < VerificationImagePolicy.MIN_IMAGE_COUNT || size > VerificationImagePolicy.MAX_IMAGE_COUNT) {
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

    private void validateFiles(List<MultipartFile> files) {
        for (MultipartFile file : files) {
            if (file.getSize() > VerificationImagePolicy.MAX_FILE_SIZE_BYTES) {
                throw new CustomException(VerificationErrorCode.IMAGE_TOO_LARGE);
            }
            String format = detectFormat(file);
            if (format == null || !VerificationImagePolicy.ALLOWED_FORMATS.contains(format)) {
                throw new CustomException(VerificationErrorCode.IMAGE_FORMAT_NOT_ALLOWED);
            }
        }
    }

    private String detectFormat(MultipartFile file) {
        try (ImageInputStream in = ImageIO.createImageInputStream(file.getInputStream())) {
            if (in == null) {
                return null;
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            return readers.hasNext() ? readers.next().getFormatName().toLowerCase() : null;
        } catch (IOException e) {
            return null;
        }
    }
}
