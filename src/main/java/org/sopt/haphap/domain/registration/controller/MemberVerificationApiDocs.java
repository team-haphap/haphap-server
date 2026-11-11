package org.sopt.haphap.domain.registration.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.haphap.domain.registration.dto.response.MemberVerificationStatusResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "합격 인증")
public interface MemberVerificationApiDocs {

    @Operation(summary = "합격 인증 현황 조회 (승인/반려)")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<SuccessResponse<MemberVerificationStatusResponse>> getVerificationStatus(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId);
}