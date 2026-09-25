package org.sopt.haphap.global.s3;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.GlobalErrorCode;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private static final String PUBLIC_CACHE = "public, max-age=31536000, immutable";
    private static final String PRIVATE_CACHE = "private, no-store";
    private static final float WEBP_QUALITY = 0.85f;

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.private-bucket}")
    private String privateBucket;

    @Value("${aws.s3.region}")
    private String region;

    @PostConstruct
    void initImageIoPlugins() {
        // 앱 시작 시 1회만 스캔 (요청마다 재스캔하지 않도록)
        ImageIO.scanForPlugins();
    }

    public String upload(MultipartFile file, String dirName) {
        String key = dirName + "/" + UUID.randomUUID() + "-" + baseName(file) + ".webp";
        putObject(bucket, key, toWebp(file), PUBLIC_CACHE);
        return "https://%s.s3.%s.amazonaws.com/%s".formatted(bucket, region, key);
    }

    public String uploadPrivate(MultipartFile file, String dirName) {
        String key = dirName + "/" + UUID.randomUUID() + ".webp";   // 원본 파일명 사용 X
        putObject(privateBucket, key, toWebp(file), PRIVATE_CACHE);
        return key;
    }

    public void delete(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }

    public void deletePrivate(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(privateBucket)
                .key(key)
                .build());
    }

    private byte[] toWebp(MultipartFile file) {
        BufferedImage image = readImage(file);
        try {
            return convertToWebp(image, WEBP_QUALITY);
        } catch (IOException e) {
            throw new CustomException(GlobalErrorCode.IMAGE_UPLOAD_FAILED);   // 변환 실패 → 서버 문제
        }
    }

    private BufferedImage readImage(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            BufferedImage image = ImageIO.read(in);
            if (image == null) {                          // 이미지가 아니거나 지원하지 않는 형식(HEIC 등)
                throw new CustomException(GlobalErrorCode.INVALID_IMAGE_FILE);
            }
            return image;
        } catch (IOException e) {
            throw new CustomException(GlobalErrorCode.INVALID_IMAGE_FILE);
        }
    }

    private void putObject(String targetBucket, String key, byte[] bytes, String cacheControl) {
        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(targetBucket).key(key)
                            .contentType("image/webp")
                            .cacheControl(cacheControl)
                            .build(),
                    RequestBody.fromBytes(bytes));
        } catch (SdkException e) {
            throw new CustomException(GlobalErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    private String baseName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        return (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(0, originalFilename.lastIndexOf('.'))
                : "file";
    }

    private byte[] convertToWebp(BufferedImage image, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("webp");
        if (!writers.hasNext()) {
            throw new IllegalStateException("WebP ImageWriter를 찾을 수 없습니다. webp-imageio 의존성을 확인하세요.");
        }
        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();

        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

            String[] types = param.getCompressionTypes();
            String lossyType = types[0];
            for (String type : types) {
                if (type.toLowerCase().contains("lossy")) {
                    lossyType = type;
                    break;
                }
            }
            param.setCompressionType(lossyType);
            param.setCompressionQuality(quality);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
        return baos.toByteArray();
    }
}