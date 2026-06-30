package org.sopt.haphap.domain.posting.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.code.PostingSuccessCode;
import org.sopt.haphap.domain.posting.dto.PostingListResponse;
import org.sopt.haphap.domain.posting.dto.PostingStageListResponse;
import org.sopt.haphap.domain.posting.service.PostingService;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/postings")
public class PostingController implements PostingApiDocs {

    private final PostingService postingService;

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
}
