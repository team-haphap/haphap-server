package org.sopt.haphap.domain.mypage.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.mypage.code.MyPageSuccessCode;
import org.sopt.haphap.domain.mypage.dto.request.MyRegistrationFilter;
import org.sopt.haphap.domain.mypage.dto.response.MyRegistrationListResponse;
import org.sopt.haphap.domain.mypage.dto.response.MyRegistrationPreviewListResponse;
import org.sopt.haphap.domain.mypage.service.MyRegistrationService;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/my-page")
@RequiredArgsConstructor
public class MyPageController implements MyPageApiDocs {

    private final MyRegistrationService myRegistrationService;

    @GetMapping("/registrations/preview")
    public ResponseEntity<SuccessResponse<MyRegistrationPreviewListResponse>> getRegistrationPreview(
            @AuthenticationPrincipal Long userId) {
        MyRegistrationPreviewListResponse response = myRegistrationService.getPreview(userId);
        return ResponseEntity.ok(ApiResponse.success(MyPageSuccessCode.MY_REGISTRATIONS_PREVIEW_FETCHED, response));
    }

    @GetMapping("/registrations")
    public ResponseEntity<SuccessResponse<MyRegistrationListResponse>> getRegistrations(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "ALL") MyRegistrationFilter filter) {
        MyRegistrationListResponse response = myRegistrationService.getAll(userId, filter);
        return ResponseEntity.ok(ApiResponse.success(MyPageSuccessCode.MY_REGISTRATIONS_FETCHED, response));
    }
}
