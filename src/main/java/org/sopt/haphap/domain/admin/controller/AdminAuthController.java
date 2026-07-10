package org.sopt.haphap.domain.admin.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.admin.dto.AdminAuthResponse;
import org.sopt.haphap.domain.admin.dto.AdminLoginRequest;
import org.sopt.haphap.domain.admin.service.AdminAuthService;
import org.sopt.haphap.global.code.AdminSuccessCode;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
@Tag(name = "admin-auth-controller", description = "클라 연동 안해도 됩니다")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<AdminAuthResponse>> login(@Valid @RequestBody AdminLoginRequest request) {
        AdminAuthResponse response = adminAuthService.login(request.loginId(), request.password());
        return ResponseEntity.ok(SuccessResponse.of(AdminSuccessCode.ADMIN_LOGIN_SUCCESS, response));
    }
}