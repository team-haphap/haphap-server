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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationImageService {
    private static final String DIR_NAME = "pass-verifications";

    private final S3Uploader s3Uploader;
    private final VerificationImageRepository verificationImageRepository;
    private final UserService userService;

    private String extractExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        }
        return "jpg";
    }

    public VerificationImageUploadResponse upload(Long userId, List<MultipartFile> files) {
        validateCount(files);
        validateFiles(files);

        User user = userService.findById(userId);
        String dirName = DIR_NAME + "/" + userId;

        List<VerificationImage> reserved = verificationImageRepository.saveAll(
                files.stream()
                        .map(file -> VerificationImage.uploadedBy(
                                user,
                                s3Uploader.newPrivateKey(dirName, extractExtension(file))))
                        .toList());

        try {
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<Future<?>> futures = new ArrayList<>();
                for (int i = 0; i < files.size(); i++) {
                    final int idx = i;
                    futures.add(executor.submit(() ->
                            s3Uploader.uploadPrivate(files.get(idx), reserved.get(idx).getS3Key())
                    ));
                }
                for (Future<?> f : futures) {
                    try {
                        f.get();
                    } catch (ExecutionException e) {
                        Throwable cause = e.getCause();
                        if (cause instanceof RuntimeException re) throw re;
                        throw new RuntimeException(cause);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                }
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
        try (InputStream in = file.getInputStream()) {
            byte[] header = in.readNBytes(8);
            if (header.length >= 2 && header[0] == (byte) 0xFF && header[1] == (byte) 0xD8) {
                return "jpeg";
            }
            if (header.length >= 8
                    && header[0] == (byte) 0x89 && header[1] == 'P'
                    && header[2] == 'N' && header[3] == 'G') {
                return "png";
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }
}
