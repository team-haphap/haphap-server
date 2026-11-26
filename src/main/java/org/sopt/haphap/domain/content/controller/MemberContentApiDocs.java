package org.sopt.haphap.domain.content.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.haphap.domain.content.dto.response.ContentListResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "콘텐츠", description = "콘텐츠 관련 API")
public interface MemberContentApiDocs {

    @Operation(summary = "내가 작성한 콘텐츠 조회", description = "로그인한 사용자가 작성한 콘텐츠 목록을 페이징하여 조회합니다.")
    @ApiResponse(responseCode = "200", description = "내가 작성한 콘텐츠 목록 조회 성공")
    ResponseEntity<SuccessResponse<ContentListResponse>> getMyContents(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(required = false) Integer size
    );
}