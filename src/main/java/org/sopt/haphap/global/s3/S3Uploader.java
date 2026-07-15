package org.sopt.haphap.global.s3;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
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

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.region}")
    private String region;

    @PostConstruct
    void initImageIoPlugins() {
        // 앱 시작 시 1회만 스캔 (요청마다 재스캔하지 않도록)
        ImageIO.scanForPlugins();
    }

    public String upload(MultipartFile file, String dirName) {
        String originalFilename = file.getOriginalFilename();
        String baseName = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(0, originalFilename.lastIndexOf('.'))
                : "file";
        String key = dirName + "/" + UUID.randomUUID() + "-" + baseName + ".webp";

        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new IllegalStateException("이미지를 읽을 수 없습니다: " + originalFilename);
            }

            byte[] webpBytes = convertToWebp(image, 0.85f);

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType("image/webp")
                            .cacheControl("public, max-age=31536000, immutable")
                            .build(),
                    RequestBody.fromBytes(webpBytes)
            );
        } catch (IOException e) {
            throw new IllegalStateException("이미지 업로드 실패", e);
        }

        return "https://%s.s3.%s.amazonaws.com/%s".formatted(bucket, region, key);
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

    public void delete(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }
}