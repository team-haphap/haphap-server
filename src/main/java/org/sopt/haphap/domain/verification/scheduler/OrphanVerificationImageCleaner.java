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
    private final OrphanVerificationImageRemover remover;
    @Scheduled(cron = "0 30 * * * *")   // 매시 30분 (하루 1번 → 매시간: 최대 보관 시간이 24h+1h로 줄어듦)
    public void clean() {
        LocalDateTime threshold = LocalDateTime.now().minus(ORPHAN_TTL);
        long lastId = 0L;
        int deleted = 0;
        int failed = 0;

        while (true) {
            List<Long> ids = verificationImageRepository.findOrphanIdsAfter(
                    threshold, lastId, PageRequest.of(0, BATCH_SIZE));
            if (ids.isEmpty()) {
                break;
            }
            for (Long id : ids) {
                try {
                    if (remover.removeIfOrphan(id, threshold)) {
                        deleted++;
                    }
                } catch (Exception e) {
                    failed++;
                    log.warn("고아 인증 이미지 삭제 실패(다음 실행에서 재시도) id={}", id, e);
                }
            }
            lastId = ids.get(ids.size() - 1);   // 실패한 건 건너뛰고 다음으로 전진
        }
        log.info("고아 인증 이미지 정리: 삭제 {}건, 실패 {}건", deleted, failed);
    }
}
