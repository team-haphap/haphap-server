package org.sopt.haphap.domain.posting.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.code.PostingSuccessCode;
import org.sopt.haphap.domain.posting.dto.response.*;
import org.sopt.haphap.domain.posting.service.*;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/postings")
public class PostingController implements PostingApiDocs {

    private final PostingService postingService;
    private final PostingViewTracker postingViewTracker;
    private final PopularPostingService popularPostingService;
    private final PostingListingService postingListingService;
    private final AnnouncementsService announcementsService;
    private final PostingDetailService postingDetailService;
    private final PostingStageStatusService postingStageStatusService;
    private final TodayStatisticService todayStatisticService;
    private final PostingStageStatisticService postingStageStatisticService;

    @GetMapping("/name")
    public ResponseEntity<SuccessResponse<PostingListResponse>> getPostings() {
        PostingListResponse response = postingService.getPostings();

        SuccessResponse<PostingListResponse> body =
                ApiResponse.success(PostingSuccessCode.POSTING_LIST_FETCHED, response);

        return ResponseEntity.status(body.status()).body(body);
    }

    @GetMapping("/{postingId}/stages")
    public ResponseEntity<SuccessResponse<PostingStageListResponse>> getStages(
            @PathVariable Long postingId
    ) {
        PostingStageListResponse response = postingService.getStages(postingId);

        SuccessResponse<PostingStageListResponse> body =
                ApiResponse.success(PostingSuccessCode.POSTING_STAGE_LIST_FETCHED, response);

        return ResponseEntity.status(body.status()).body(body);
    }

    @PatchMapping("/{postingId}/views")
    public ResponseEntity<Void> recordView(@PathVariable Long postingId) {
        postingViewTracker.recordView(postingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<PopularPostingListResponse>> getPopularPostings(
            @RequestParam(required = false) List<String> category
    ) {
        PopularPostingListResponse response = popularPostingService.getPopularPostings(category);
        SuccessResponse<PopularPostingListResponse> body =
                ApiResponse.success(PostingSuccessCode.POPULAR_POSTINGS_FETCHED, response);
        return ResponseEntity.status(body.status()).body(body);
    }

    @GetMapping("/all")
    public ResponseEntity<SuccessResponse<PopularPostingListResponse>> getAllPostings(
            @RequestParam(required = false) List<String> category
    ) {
        PopularPostingListResponse response = postingListingService.getAllPostings(category);
        SuccessResponse<PopularPostingListResponse> body =
                ApiResponse.success(PostingSuccessCode.POSTING_ALL_LIST_FETCHED, response);
        return ResponseEntity.status(body.status()).body(body);
    }

    @PatchMapping("/{postingId}/card-clicks")
    public ResponseEntity<Void> recordCardClick(@PathVariable Long postingId) {
        postingViewTracker.recordCardClick(postingId);
        return ResponseEntity.noContent().build();
    }
  
    @GetMapping("/announcements")
    public ResponseEntity<SuccessResponse<TodayAnnouncementPostingListResponse>> getTodayAnnouncementPostings() {
        TodayAnnouncementPostingListResponse response = announcementsService.getTodayAnnouncementPostings();

        SuccessResponse<TodayAnnouncementPostingListResponse> body =
                ApiResponse.success(PostingSuccessCode.TODAY_ANNOUNCEMENT_POSTING_FETCHED, response);

        return ResponseEntity.status(body.status()).body(body);
    }

    @GetMapping("/{postingId}/detail")
    public ResponseEntity<SuccessResponse<PostingDetailResponse>> getDetail(@AuthenticationPrincipal Long userId, @PathVariable Long postingId) {
        PostingDetailResponse response = postingDetailService.getDetail(userId,postingId);

        SuccessResponse<PostingDetailResponse> body =
                ApiResponse.success(PostingSuccessCode.POSTING_DETAIL_FETCHED, response);

        return ResponseEntity.status(body.status()).body(body);
    }

    @GetMapping("/{postingId}/statistics")
    public ResponseEntity<SuccessResponse<PostingStageStatusListResponse>> getStagesStatus(@PathVariable Long postingId) {
        PostingStageStatusListResponse response = postingStageStatusService.getStagesStatus(postingId);

        SuccessResponse<PostingStageStatusListResponse> body =
                ApiResponse.success(PostingSuccessCode.POSTING_STAGE_STATUS_FETCHED, response);

        return ResponseEntity.status(body.status()).body(body);
    }

    @GetMapping("/{postingId}/{stageId}/statistics")
    public ResponseEntity<SuccessResponse<PostingStageStatisticResponse>> getStagesStatistic(
            @PathVariable Long postingId,
            @PathVariable Long stageId
    ){
        PostingStageStatisticResponse response = postingStageStatisticService.getPostingStageStatistic(postingId,stageId);
        SuccessResponse<PostingStageStatisticResponse> body =
                ApiResponse.success(PostingSuccessCode.POSTING_STAGE_STATISTIC_FETCHED, response);

        return ResponseEntity.status(body.status()).body(body);
    }

    @GetMapping("/today")
    public ResponseEntity<SuccessResponse<TodayStatisticResponse>> getTodayStatistics() {
        TodayStatisticResponse response = todayStatisticService.getTodayStatistics();

        SuccessResponse<TodayStatisticResponse> body =
                ApiResponse.success(PostingSuccessCode.TODAY_STATISTIC_FETCHED,response);

        return ResponseEntity.status(body.status()).body(body);
    }

}
