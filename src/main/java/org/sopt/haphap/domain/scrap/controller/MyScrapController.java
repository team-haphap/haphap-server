package org.sopt.haphap.domain.scrap.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.scrap.code.ScrapSuccessCode;
import org.sopt.haphap.domain.scrap.dto.response.MyScrapListResponse;
import org.sopt.haphap.domain.scrap.service.ScrapService;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 마이페이지 화면의 찜 목록. 경로는 /my-page 아래지만 찜 도메인 소관이라 scrap 패키지에 둔다.
@RestController
@RequestMapping("/api/v1/my-page")
@RequiredArgsConstructor
public class MyScrapController implements MyScrapApiDocs {

    private final ScrapService scrapService;

    @GetMapping("/scraps")
    public ResponseEntity<SuccessResponse<MyScrapListResponse>> getMyScraps(
            @AuthenticationPrincipal Long userId) {
        MyScrapListResponse response = scrapService.getMyScraps(userId);
        return ResponseEntity.ok(ApiResponse.success(ScrapSuccessCode.MY_SCRAPS_FETCHED, response));
    }
}
