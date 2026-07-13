package org.sopt.haphap.domain.posting.controller;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.code.PostingSuccessCode;
import org.sopt.haphap.domain.posting.dto.request.PostingCreateRequest;
import org.sopt.haphap.domain.posting.dto.response.PostingAdminResponse;
import org.sopt.haphap.domain.posting.service.AdminPostingService;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/postings")
@RequiredArgsConstructor
@Hidden
public class AdminPostingController {

    private final AdminPostingService adminPostingService;

    @PostMapping
    public ResponseEntity<SuccessResponse<PostingAdminResponse>> createPosting(
            @Valid @RequestBody PostingCreateRequest request) {
        SuccessResponse<PostingAdminResponse> body =
                ApiResponse.success(PostingSuccessCode.POSTING_CREATED, adminPostingService.createPosting(request));
        return ResponseEntity.status(body.status()).body(body);
    }
}