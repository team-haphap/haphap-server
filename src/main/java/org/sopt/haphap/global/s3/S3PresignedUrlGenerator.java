package org.sopt.haphap.global.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;

@Component
@RequiredArgsConstructor
public class S3PresignedUrlGenerator {

    private static final Duration EXPIRY = Duration.ofMinutes(5);

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.private-bucket}")
    private String privateBucket;

    /** 비공개 파일을 EXPIRY 동안만 볼 수 있는 임시 URL */
    public String generateGetUrl(String key) {
        GetObjectPresignRequest request = GetObjectPresignRequest.builder()
                .signatureDuration(EXPIRY)
                .getObjectRequest(builder -> builder.bucket(privateBucket).key(key))
                .build();
        return s3Presigner.presignGetObject(request).url().toString();
    }
}