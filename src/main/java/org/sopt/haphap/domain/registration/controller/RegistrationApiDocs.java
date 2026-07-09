package org.sopt.haphap.domain.registration.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.sopt.haphap.domain.registration.dto.request.RegistrationCreateRequest;
import org.sopt.haphap.domain.registration.dto.response.RegistrationCreateResponse;
import org.sopt.haphap.global.dto.FailureResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "상태 등록",description = "사용자의 합/불/대기 상태 등록을 위한 API ")
public interface RegistrationApiDocs {

    @ApiResponse(
            responseCode = "400",
            description = """
        - PENDING_MUST_NOT_HAVE_CONTACT : 대기 상태에서는 연락 정보를 보낼 수 없습니다.
        - CONFIRMED_MUST_HAVE_CONTACT : 합격/불합격 결과에는 연락 정보가 필요합니다.
        - DUPLICATE_REGISTRATION : 이미 입력한 전형입니다.
        """,
            content = @Content(
                    schema = @Schema(implementation = FailureResponse.class)
            )
    )
    @Operation(summary = "합불 현황 등록" ,
            description = """
                공고 . 전형 별 사용자의 상태를 등록하고 알람 여부를 설정합니다.
                - PENDING 상태 인 경우 contactMethod와 contactAt필드를 null로 해주세요
                """)
    ResponseEntity<SuccessResponse<RegistrationCreateResponse>> createRegistration(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Valid @RequestBody RegistrationCreateRequest request);

    @ApiResponse(
            responseCode = "400",
            description = """
        - POSTING_NOT_FOUND : 존재하지 않는 공고입니다.
        - STAGE_NOT_FOUND : 존재하지 않는 전형입니다.
        - STAGE_NOT_IN_POSTING : 해당 공고의 전형 단계가 아닙니다.
        - DUPLICATE_REGISTRATION : 이미 입력한 전형입니다.
        """,
            content = @Content(
                    schema = @Schema(implementation = FailureResponse.class)
            )
    )
    @Operation(summary = "해당 공고 동일 전형 등록 여부 조회(등록 유효성 검증)" ,
            description = """
                해당 공고의 동일 전형에 이미 등록하였는지 여부를 검증합니다. .
                - '아직 몰라요' 외의 다른 데이터가 있는 경우 409 에러로 처리합니다. 
                """)
    ResponseEntity<SuccessResponse<Void>> check(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable Long postingId,
            @PathVariable Long stageId);
}