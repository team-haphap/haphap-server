package org.sopt.haphap.domain.registration.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.haphap.domain.registration.dto.response.VerificationSummaryResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "합격 인증")
public interface MemberVerificationApiDocs {

    @Operation(summary = "합격 인증 현황 조회 (승인/반려)",
            description = "본인의 합격 인증 요청을 최신순으로 반환합니다. 인증 대상(PASS)이 아닌 등록은 포함되지 않으며, 반려 건은 resubmittable=true 입니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<SuccessResponse<VerificationSummaryResponse>> getSummary(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId);
}