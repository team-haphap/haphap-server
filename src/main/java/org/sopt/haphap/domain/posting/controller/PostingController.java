package org.sopt.haphap.domain.posting.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.code.PostingSuccessCode;
import org.sopt.haphap.domain.posting.dto.response.PopularPostingListResponse;
import org.sopt.haphap.domain.posting.dto.response.PostingListResponse;
import org.sopt.haphap.domain.posting.dto.response.PostingStageListResponse;
import org.sopt.haphap.domain.posting.dto.response.TodayAnnouncementPostingListResponse;
import org.sopt.haphap.domain.posting.service.AnnouncementsService;
import org.sopt.haphap.domain.posting.service.PopularPostingService;
import org.sopt.haphap.domain.posting.service.PostingListingService;
import org.sopt.haphap.domain.posting.service.PostingService;
import org.sopt.haphap.domain.posting.service.PostingViewTracker;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
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
}
