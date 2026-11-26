package org.sopt.haphap.domain.registration.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.haphap.domain.registration.dto.request.VerificationResubmitRequest;
import org.sopt.haphap.domain.registration.dto.response.VerificationResubmitResponse;
import org.sopt.haphap.global.dto.FailureResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "합격 인증")
public interface RegistrationVerificationApiDocs {

    @Operation(summary = "합격 인증 이미지 재등록 (반려 건 재제출)",
            description = "반려(REJECTED)된 본인 등록에만 가능합니다. 성공 시 승인 대기(PENDING)로 돌아가고 운영진에게 재검토 요청이 전달됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재등록 성공"),
            @ApiResponse(responseCode = "400", description = "이미지 개수 오류(IMAGE_COUNT_INVALID)",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class))),
            @ApiResponse(responseCode = "404", description = "등록/이미지 없음",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class))),
            @ApiResponse(responseCode = "409", description = "반려 건이 아님/이미 연결된 이미지",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class)))
    })
    ResponseEntity<SuccessResponse<VerificationResubmitResponse>> resubmit(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "등록 ID") Long registrationId,
            VerificationResubmitRequest request);
}