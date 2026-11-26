package org.sopt.haphap.domain.verification.scheduler;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.verification.entity.VerificationImage;
import org.sopt.haphap.domain.verification.repository.VerificationImageRepository;
import org.sopt.haphap.global.s3.S3Uploader;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OrphanVerificationImageRemover {

    private final VerificationImageRepository verificationImageRepository;
    private final S3Uploader s3Uploader;

    // 잠금을 건 뒤 아직도 고아인지 다시 확인하고 지운다. 지웠으면 true
    @Transactional
    public boolean removeIfOrphan(Long imageId, LocalDateTime threshold) {
        VerificationImage image = verificationImageRepository.findByIdForUpdate(imageId).orElse(null);
        if (image == null || image.isAttached() || !image.getCreatedAt().isBefore(threshold)) {
            return false;   // 이미 지워졌거나, 그 사이 등록에 연결됐거나, 아직 24시간 안 됨
        }
        s3Uploader.deletePrivate(image.getS3Key());   // S3 실패 → 예외 → 롤백 → 행이 남아 다음 배치에서 재시도
        verificationImageRepository.delete(image);
        return true;
    }
}