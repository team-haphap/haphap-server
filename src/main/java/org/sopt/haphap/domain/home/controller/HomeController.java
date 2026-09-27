package org.sopt.haphap.domain.home.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.home.code.HomeSuccessCode;
import org.sopt.haphap.domain.home.dto.response.MyApplicationListResponse;
import org.sopt.haphap.domain.home.service.MyApplicationService;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final MyApplicationService myApplicationService;

    @GetMapping("/my-applications")
    public ResponseEntity<SuccessResponse<MyApplicationListResponse>> getMyApplications(
            @AuthenticationPrincipal Long userId) {
        MyApplicationListResponse response = myApplicationService.getMyApplications(userId);
        return ResponseEntity.ok(ApiResponse.success(HomeSuccessCode.MY_APPLICATIONS_FETCHED, response));
    }
}
