package org.sopt.haphap.domain.verification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.haphap.domain.verification.dto.response.VerificationImageUploadResponse;
import org.sopt.haphap.global.dto.FailureResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "합격 인증", description = "합격 인증 이미지 업로드 API")
public interface VerificationImageApiDocs {

    @Operation(summary = "합격 인증 이미지 업로드",
            description = """
                합격 결과 등록 전에 인증 이미지를 먼저 업로드합니다.
                - files: 이미지 1~3장 (JPG/PNG, 장당 10MB 이하)
                - 응답의 `imageIds`를 결과 등록 API의 `verificationImageIds`에 넣어주세요.
                - 업로드 후 24시간 안에 결과 등록에 사용하지 않은 이미지는 자동 삭제됩니다.
                - 이미지는 비공개로 저장되며 운영진만 확인할 수 있습니다.
                """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "업로드 성공",
                    content = @Content(examples = @ExampleObject(value = """
                        { "status": 201, "code": "VERIFICATION_IMAGE_UPLOADED",
                          "message": "인증 이미지가 업로드되었습니다.", "data": { "imageIds": [11, 12] } }
                        """))),
            @ApiResponse(responseCode = "400", description = "이미지 개수 오류 / 읽을 수 없는 파일",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class))),
            @ApiResponse(responseCode = "413", description = "파일 크기 초과",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class))),
            @ApiResponse(responseCode = "503", description = "S3 업로드 실패 (재시도)",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class)))
    })
    ResponseEntity<SuccessResponse<VerificationImageUploadResponse>> upload(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "인증 이미지 1~3장") List<MultipartFile> files);
}
