package org.sopt.haphap.domain.posting.controller;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.code.PostingSuccessCode;
import org.sopt.haphap.domain.posting.dto.request.CategoryCreateRequest;
import org.sopt.haphap.domain.posting.dto.response.CategoryResponse;
import org.sopt.haphap.domain.posting.service.AdminCategoryService;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@Hidden
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    @PostMapping
    public ResponseEntity<SuccessResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryCreateRequest request) {
        SuccessResponse<CategoryResponse> body =
                ApiResponse.success(PostingSuccessCode.CATEGORY_CREATED, adminCategoryService.createCategory(request));
        return ResponseEntity.status(body.status()).body(body);
    }
}