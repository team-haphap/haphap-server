package org.sopt.haphap.domain.mypage.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.mypage.code.MyPageSummarySuccessCode;
import org.sopt.haphap.domain.mypage.dto.response.MyPageSummaryResponse;
import org.sopt.haphap.domain.mypage.service.MyPageSummaryService;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/my-page")
@RequiredArgsConstructor
public class MyPageSummaryController implements MyPageSummaryApiDocs {

    private final MyPageSummaryService myPageSummaryService;

    @GetMapping
    public ResponseEntity<SuccessResponse<MyPageSummaryResponse>> getSummary(
            @AuthenticationPrincipal Long userId) {
        MyPageSummaryResponse response = myPageSummaryService.getSummary(userId);
        return ResponseEntity.ok(ApiResponse.success(MyPageSummarySuccessCode.MY_PAGE_SUMMARY_FETCHED, response));
    }
}
