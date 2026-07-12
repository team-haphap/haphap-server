package org.sopt.haphap.global.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.haphap.global.dto.FailureResponse; // 프로젝트 구조에 맞게 패키지 확인 필요
import org.sopt.haphap.global.dto.ImageUploadResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "이미지", description = "이미지 업로드 관련 API 입니다")
public interface ImageApiDocs {

    @Operation(summary = "이미지 업로드",
            description = """
                    이미지 파일을 S3에 업로드하고 접근 가능한 URL을 반환합니다.
                    로그인한 사용자만 호출할 수 있습니다.
                    파일 용량은 최대 10MB까지 허용되며, 초과 시 413 응답을 반환합니다.
                    """)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "업로드 성공",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                              "status": 201,
                              "code": "IMAGE_UPLOADED",
                              "message": "이미지가 업로드되었습니다.",
                              "data": { "imageUrl": "https://.../uploaded-image.png" }
                            }
                            """))
            ),
            @ApiResponse(
                    responseCode = "413",
                    description = "업로드 가능한 파일 크기(10MB) 초과",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class))
            )
    })
    ResponseEntity<SuccessResponse<ImageUploadResponse>> uploadImage(
            @Parameter(description = "업로드할 이미지 파일") MultipartFile file);
}