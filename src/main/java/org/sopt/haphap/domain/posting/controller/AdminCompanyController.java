package org.sopt.haphap.domain.posting.controller;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.code.PostingSuccessCode;
import org.sopt.haphap.domain.posting.dto.request.CompanyCreateRequest;
import org.sopt.haphap.domain.posting.dto.response.CompanyResponse;
import org.sopt.haphap.domain.posting.service.AdminCompanyService;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/companies")
@RequiredArgsConstructor
@Hidden
public class AdminCompanyController {

    private final AdminCompanyService adminCompanyService;

    @PostMapping
    public ResponseEntity<SuccessResponse<CompanyResponse>> createCompany(
            @Valid @RequestBody CompanyCreateRequest request) {
        SuccessResponse<CompanyResponse> body =
                ApiResponse.success(PostingSuccessCode.COMPANY_CREATED, adminCompanyService.createCompany(request));
        return ResponseEntity.status(body.status()).body(body);
    }
}