package org.sopt.haphap.global.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.haphap.global.dto.ImageBulkUploadResponse;
import org.sopt.haphap.global.dto.ImageUploadResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.sopt.haphap.global.s3.ImageCategory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "이미지", description = "이미지 업로드 관련 API 입니다")
public interface ImageApiDocs {

    @Operation(summary = "이미지 업로드",
            description = """
                    이미지 파일을 카테고리별 S3 경로에 업로드하고 접근 가능한 URL을 반환합니다.
                    로그인한 사용자만 호출할 수 있습니다.
                    파일 용량은 최대 10MB까지 허용되며, 초과 시 413 응답을 반환합니다.
                    category는 LOGO_IMAGE, CARD_LOGO, IMAGE, PASS_CARD, BANNER 중 하나여야 합니다.
                    """)
    ResponseEntity<SuccessResponse<ImageUploadResponse>> uploadImage(
            @Parameter(description = "업로드할 이미지 파일") MultipartFile file,
            @Parameter(description = "이미지 카테고리 (LOGO_IMAGE, CARD_LOGO, IMAGE, PASS_CARD, BANNER)") ImageCategory category);

    @Operation(summary = "이미지 일괄 업로드",
            description = """
                여러 이미지 파일을 한 번에 같은 카테고리로 S3에 업로드하고 URL 목록을 반환합니다.
                반환 순서는 업로드 순서와 동일합니다.
                """)
    ResponseEntity<SuccessResponse<ImageBulkUploadResponse>> uploadImages(
            @Parameter(description = "업로드할 이미지 파일 목록") List<MultipartFile> files,
            @Parameter(description = "이미지 카테고리") ImageCategory category);

}