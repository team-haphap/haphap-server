package org.sopt.haphap.domain.posting.controller;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.code.PostingSuccessCode;
import org.sopt.haphap.domain.posting.dto.request.PostingStageCreateRequest;
import org.sopt.haphap.domain.posting.dto.response.PostingStageAdminResponse;
import org.sopt.haphap.domain.posting.service.AdminPostingStageService;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/postings/{postingId}/stages")
@RequiredArgsConstructor
@Hidden
public class AdminPostingStageController {

    private final AdminPostingStageService adminPostingStageService;

    @PostMapping
    public ResponseEntity<SuccessResponse<PostingStageAdminResponse>> createStage(
            @PathVariable Long postingId, @Valid @RequestBody PostingStageCreateRequest request) {
        SuccessResponse<PostingStageAdminResponse> body = ApiResponse.success(
                PostingSuccessCode.POSTING_STAGE_CREATED, adminPostingStageService.createStage(postingId, request));
        return ResponseEntity.status(body.status()).body(body);
    }
}