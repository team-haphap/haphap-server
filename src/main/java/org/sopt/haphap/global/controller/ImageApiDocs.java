package org.sopt.haphap.global.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.haphap.global.dto.ImageUploadResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.sopt.haphap.global.s3.ImageCategory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "이미지", description = "이미지 업로드 관련 API 입니다")
public interface ImageApiDocs {

    @Hidden
    ResponseEntity<SuccessResponse<ImageUploadResponse>> uploadImage(MultipartFile file, ImageCategory category);
}