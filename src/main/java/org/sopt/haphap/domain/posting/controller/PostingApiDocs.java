package org.sopt.haphap.domain.posting.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.haphap.domain.posting.dto.PopularPostingListResponse;
import org.sopt.haphap.domain.posting.dto.PostingListResponse;
import org.sopt.haphap.domain.posting.dto.PostingStageListResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "공고",description = "공고관련 API 입니다")
public interface PostingApiDocs {
    @Operation(summary = "공고명 리스트 조회 " ,description = "전체 공고명을 가나다 순으로 반환합니다.")
    ResponseEntity<SuccessResponse<PostingListResponse>> getPostings();

    @Operation(summary = "공고 별 전형 조회",description = "해당 공고의 전형을 반환합니다.")
    ResponseEntity<SuccessResponse<PostingStageListResponse>> getStages(@PathVariable Long postingId);

    @Operation(summary = "홈 메인-최근 등록 공고 조회", description = "홈 메인화면에서 최근 등록 공고 8개를 반환합니다.(48내 등록 건수 많은 순으로 반환)")
    ResponseEntity<SuccessResponse<PopularPostingListResponse>> getPopularPostings(@RequestParam(required = false) List<String> category);

    @Operation(summary = "공고 리스트 조회",description = "공고 리스트 전체보기에서 마감일 임박 순으로 전체 공고를 반환합니다.")
    ResponseEntity<SuccessResponse<PopularPostingListResponse>> getAllPostings(@RequestParam(required = false) List<String> category);
}
