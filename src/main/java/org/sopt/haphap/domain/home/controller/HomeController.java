package org.sopt.haphap.domain.home.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.home.code.HomeSuccessCode;
import org.sopt.haphap.domain.home.dto.response.MyApplicationListResponse;
import org.sopt.haphap.domain.home.dto.response.RecentViewListResponse;
import org.sopt.haphap.domain.home.service.HomePopularPostingService;
import org.sopt.haphap.domain.home.service.MyApplicationService;
import org.sopt.haphap.domain.home.service.RecentViewService;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final MyApplicationService myApplicationService;
    private final RecentViewService recentViewService;
    private final HomePopularPostingService homePopularPostingService;

    @GetMapping("/my-applications")
    public ResponseEntity<SuccessResponse<MyApplicationListResponse>> getMyApplications(
            @AuthenticationPrincipal Long userId) {
        MyApplicationListResponse response = myApplicationService.getMyApplications(userId);
        return ResponseEntity.ok(ApiResponse.success(HomeSuccessCode.MY_APPLICATIONS_FETCHED, response));
    }

    @GetMapping("/recent-views")
    public ResponseEntity<SuccessResponse<RecentViewListResponse>> getRecentViews(
            @AuthenticationPrincipal Long userId) {
        RecentViewListResponse response = recentViewService.getRecentViews(userId);
        return ResponseEntity.ok(ApiResponse.success(HomeSuccessCode.RECENT_VIEWS_FETCHED, response));
    }

    @GetMapping("/popular-postings")
    public ResponseEntity<SuccessResponse<RecentViewListResponse>> getPopularPostings(
            @RequestParam(required = false) List<String> category) {
        RecentViewListResponse response = homePopularPostingService.getPopularPostings(category);
        return ResponseEntity.ok(ApiResponse.success(HomeSuccessCode.POPULAR_POSTINGS_FETCHED, response));
    }
}
