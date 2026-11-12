package org.sopt.haphap.domain.content.controller;

import org.sopt.haphap.domain.content.dto.response.ContentCreateResponse;
import org.sopt.haphap.global.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contents")
@RequiredArgsConstructor
public class ContentController implements ContentApiDocs {

    private final ContentService contentService;
    private final ContentQueryService contentQueryService;

    @Override
    @PostMapping
    public ResponseEntity<SuccessResponse<ContentCreateResponse>> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ContentCreateRequest request) {
        SuccessResponse<ContentCreateResponse> body = ApiResponse.success(
                ContentSuccessCode.CONTENT_CREATED, contentService.create(userId, request));
        return ResponseEntity.status(body.status()).body(body);
    }

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse<ContentListResponse>> explore(
            @RequestParam(required = false) ContentType type,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        SuccessResponse<ContentListResponse> body = ApiResponse.success(
                ContentSuccessCode.CONTENT_LIST_FETCHED, contentQueryService.explore(type, page, size));
        return ResponseEntity.status(body.status()).body(body);
    }

    @Override
    @GetMapping("/{contentId}")
    public ResponseEntity<SuccessResponse<ContentDetailResponse>> getDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long contentId) {
        SuccessResponse<ContentDetailResponse> body = ApiResponse.success(
                ContentSuccessCode.CONTENT_FETCHED, contentQueryService.getDetail(userId, contentId));
        return ResponseEntity.status(body.status()).body(body);
    }
}