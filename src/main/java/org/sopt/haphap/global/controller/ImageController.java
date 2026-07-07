package org.sopt.haphap.global.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.ImageSuccessCode;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.ImageUploadResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.sopt.haphap.global.s3.S3Uploader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/images")
public class ImageController implements ImageApiDocs{

    private final S3Uploader s3Uploader;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<SuccessResponse<ImageUploadResponse>> uploadImage(
            @RequestParam("file") MultipartFile file
    ) {
        String url = s3Uploader.upload(file, "images");
        SuccessResponse<ImageUploadResponse> body =
                ApiResponse.success(ImageSuccessCode.IMAGE_UPLOADED, new ImageUploadResponse(url));
        return ResponseEntity.status(body.status()).body(body);
    }
}