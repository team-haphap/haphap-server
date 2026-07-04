package org.sopt.haphap.domain.posting.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.code.PostingSuccessCode;
import org.sopt.haphap.domain.posting.dto.PostingListResponse;
import org.sopt.haphap.domain.posting.dto.PostingStageListResponse;
import org.sopt.haphap.domain.posting.service.PostingService;
import org.sopt.haphap.domain.posting.service.PostingViewTracker;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/postings")
public class PostingController implements PostingApiDocs {

    private final PostingService postingService;
    private final PostingViewTracker postingViewTracker;

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

    @PatchMapping("/{postingId}/card-clicks")
    public ResponseEntity<Void> recordCardClick(@PathVariable Long postingId) {
        postingViewTracker.recordCardClick(postingId);
        return ResponseEntity.noContent().build();
    }
}
