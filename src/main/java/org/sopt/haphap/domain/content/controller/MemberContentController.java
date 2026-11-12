package org.sopt.haphap.domain.content.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.content.dto.response.ContentListResponse;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members/me/contents")
@RequiredArgsConstructor
public class MemberContentController implements MemberContentApiDocs {

    private final ContentQueryService contentQueryService;

    @GetMapping
    public ResponseEntity<SuccessResponse<ContentListResponse>> getMyContents(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        SuccessResponse<ContentListResponse> body = ApiResponse.success(
                ContentSuccessCode.MY_CONTENT_LIST_FETCHED,
                contentQueryService.getMyContents(userId, page, size));
        return ResponseEntity.status(body.status()).body(body);
    }
}