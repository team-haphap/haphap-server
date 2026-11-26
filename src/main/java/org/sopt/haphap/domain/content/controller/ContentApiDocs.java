package org.sopt.haphap.domain.content.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.sopt.haphap.domain.content.domain.ContentType;
import org.sopt.haphap.domain.content.dto.request.ContentCreateRequest;
import org.sopt.haphap.domain.content.dto.response.ContentCreateResponse;
import org.sopt.haphap.domain.content.dto.response.ContentDetailResponse;
import org.sopt.haphap.domain.content.dto.response.ContentListResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "콘텐츠", description = "콘텐츠 관련 API")
public interface ContentApiDocs {

    @Operation(summary = "콘텐츠 등록", description = "새로운 콘텐츠를 등록합니다.")
    @ApiResponse(responseCode = "201", description = "콘텐츠 등록 성공")
    ResponseEntity<SuccessResponse<ContentCreateResponse>> create(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ContentCreateRequest request
    );

    @Operation(summary = "콘텐츠 목록 조회 (탐색)", description = "콘텐츠 타입별 또는 전체 콘텐츠 목록을 페이징하여 조회합니다.")
    @ApiResponse(responseCode = "200", description = "콘텐츠 목록 조회 성공")
    ResponseEntity<SuccessResponse<ContentListResponse>> explore(
            @Parameter(description = "콘텐츠 타입 필터 (미입력 시 전체 조회)") @RequestParam(required = false) ContentType type,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(required = false) Integer size
    );

    @Operation(summary = "콘텐츠 상세 조회", description = "특정 콘텐츠의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "콘텐츠 상세 조회 성공")
    ResponseEntity<SuccessResponse<ContentDetailResponse>> getDetail(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "조회할 콘텐츠 ID", example = "1") @PathVariable Long contentId
    );
}
