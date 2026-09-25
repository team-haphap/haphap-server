package org.sopt.haphap.domain.verification.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.verification.entity.VerificationImage;
import org.sopt.haphap.domain.verification.repository.VerificationImageRepository;
import org.sopt.haphap.global.s3.S3Uploader;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrphanVerificationImageCleaner {

    private static final Duration ORPHAN_TTL = Duration.ofHours(24);
    private static final int BATCH_SIZE = 500;

    private final VerificationImageRepository verificationImageRepository;
    private final S3Uploader s3Uploader;

    @Scheduled(cron = "0 30 4 * * *")   // 매일 04:30 (참고 - 04:00 집계 보정 배치)
    public void clean() {
        LocalDateTime threshold = LocalDateTime.now().minus(ORPHAN_TTL);
        List<VerificationImage> orphans =
                verificationImageRepository.findOrphans(threshold, PageRequest.of(0, BATCH_SIZE));

        int deleted = 0;
        for (VerificationImage image : orphans) {
            try {
                s3Uploader.deletePrivate(image.getS3Key());
                verificationImageRepository.delete(image);
                deleted++;
            } catch (Exception e) {
                log.warn("고아 인증 이미지 삭제 실패 id={}", image.getId(), e);
            }
        }
        log.info("고아 인증 이미지 정리: 대상 {}건, 삭제 {}건", orphans.size(), deleted);
    }
}
// S3 먼저 지우고, DB 지우기
