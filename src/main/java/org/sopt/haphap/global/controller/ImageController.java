package org.sopt.haphap.global.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.ImageSuccessCode;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.ImageBulkUploadResponse;
import org.sopt.haphap.global.dto.ImageUploadResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.sopt.haphap.global.s3.ImageCategory;
import org.sopt.haphap.global.s3.S3Uploader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/images")
public class ImageController implements ImageApiDocs{

    private final S3Uploader s3Uploader;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<SuccessResponse<ImageUploadResponse>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") ImageCategory category
    ) {
        String url = s3Uploader.upload(file, category.getDirName());
        SuccessResponse<ImageUploadResponse> body =
                ApiResponse.success(ImageSuccessCode.IMAGE_UPLOADED, new ImageUploadResponse(url));
        return ResponseEntity.status(body.status()).body(body);
    }

    @PostMapping(value = "/bulk", consumes = "multipart/form-data")
    public ResponseEntity<SuccessResponse<ImageBulkUploadResponse>> uploadImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("category") ImageCategory category
    ) {
        List<String> urls = files.stream()
                .map(file -> s3Uploader.upload(file, category.getDirName()))
                .toList();

        SuccessResponse<ImageBulkUploadResponse> body =
                ApiResponse.success(ImageSuccessCode.IMAGE_UPLOADED, new ImageBulkUploadResponse(urls));
        return ResponseEntity.status(body.status()).body(body);
    }
}