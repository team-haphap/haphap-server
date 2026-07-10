package org.sopt.haphap.domain.posting.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.haphap.domain.posting.code.PostingSuccessCode;
import org.sopt.haphap.domain.posting.dto.response.*;
import org.sopt.haphap.global.dto.FailureResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.List;

@Tag(name = "공고",description = "공고관련 API 입니다")
public interface PostingApiDocs {
    @Operation(summary = "공고 리스트 이름 조회", description = "전체 공고명을 가나다 순으로 반환합니다.")
    ResponseEntity<SuccessResponse<PostingListResponse>> getPostings();

    @ApiResponse(
            responseCode = "404",
            description = """
        - POSTING_NOT_FOUND : 존재하지 않는 공고입니다.
        """,
            content = @Content(
                    schema = @Schema(implementation = FailureResponse.class)
            )
    )
    @Operation(summary = "공고 별 전형 조회", description = "해당 공고의 전형을 반환합니다.")
    ResponseEntity<SuccessResponse<PostingStageListResponse>> getStages(@PathVariable Long postingId);

    @Operation(summary = "공고 상세 조회 기록",
            description = "상세 페이지 진입 시 호출합니다. 인기 공고 집계에 사용되며 응답 본문은 없습니다.")
    @ApiResponse(responseCode = "204", description = "기록 성공, 응답 본문 없음")
    ResponseEntity<Void> recordView(@PathVariable Long postingId);

    @Operation(summary = "공고 카드 클릭 기록",
            description = """
                    홈/리스트/인기 공고 섹션 등 공고 카드가 노출되는 모든 화면에서 카드를 클릭했을 때 호출합니다.
                    상세 페이지 진입 기록과 합산되어 인기 공고 집계(카드 클릭률 + 상세페이지 진입자 수)에 사용되며,
                    응답 본문은 없습니다.
                    """)
    @ApiResponse(responseCode = "204", description = "기록 성공, 응답 본문 없음")
    ResponseEntity<Void> recordCardClick(@PathVariable Long postingId);
               
    @Operation(summary = "홈 메인-최근 등록 공고 조회(카테고리 별 공고조회)",
            description = """
                    홈 메인화면에서 최근 등록 공고 8개를 반환합니다.(48내 등록 건수 많은 순으로 반환)
                    - '전체' 선택한 경우 파라미터를 붙이지 말아주세요!
                    - 카테고리를 , 기준으로 보내주세요!
                    """
    )
    ResponseEntity<SuccessResponse<PopularPostingListResponse>> getPopularPostings(@RequestParam(required = false) String category);

    @Operation(summary = "카테고리별 공고 전체 조회",
            description = """
                    공고 리스트 전체보기에서 마감일 임박 순으로 전체 공고를 반환합니다.
                    - '전체' 선택한 경우 파라미터를 붙이지 말아주세요!
                    - 카테고리를 , 기준으로 보내주세요!
                    """
    )
    ResponseEntity<SuccessResponse<PopularPostingListResponse>> getAllPostings(@RequestParam(required = false) String category);

    @Operation(summary = "오늘 발표 예상 공고조회",
            description = """
                    오늘 발표 예상 전형이 있는 공고들을 조회합니다.
                    - score이 높은 순 3개 반환합니다.
                    """
    )
    ResponseEntity<SuccessResponse<TodayAnnouncementPostingListResponse>> getTodayAnnouncementPostings();

    @ApiResponse(
            responseCode = "404",
            description = """
        - POSTING_NOT_FOUND : 존재하지 않는 공고입니다.
        """,
            content = @Content(
                    schema = @Schema(implementation = FailureResponse.class)
            )
    )
    @Operation(summary = "공고 상세 조회",
            description = """
                    공고 상세 조회합니다. 
                    - 각 공고에 등록중이 회원 수를 표시합니다. (한 유저가 중복 등록한 경우 중복 제거)
                    - 프로필 이미지는 최신 등록 순으로 제시합니다. 
                    - 해당 공고에 실시간으로 등록한 전형을 제보합니다.(최대 30개 제시)
                    """
    )
    ResponseEntity<SuccessResponse<PostingDetailResponse>> getDetail(@PathVariable Long postingId);

    @ApiResponse(
            responseCode = "404",
            description = """
        - POSTING_NOT_FOUND : 존재하지 않는 공고입니다.
        """,
            content = @Content(
                    schema = @Schema(implementation = FailureResponse.class)
            )
    )
    @Operation(summary = "공고 상세 - 공고 전형별 상태 조회",
            description = """
                    공고 전형별 상태를 조회합니다. 
                    - currentStageId 는 현재 진행중인 상태입니다.(없으면 null)
                    """
    )
    ResponseEntity<SuccessResponse<PostingStageStatusListResponse>> getStagesStatus(@PathVariable Long postingId);

    @Operation(summary = "오늘 집계 조회",
            description = """
                    오늘의 집계를 조회합니다. 
                    - cumulatedCount: 오늘 등록된 모든 합/불/대기중 결과 (변경 포함!)
                    - onGoingCount: 마감되지 않은 공고들 모두
                    - 오늘 발표되었다고 판단된 전형이 있는 모든 공고
                    """
    )
    ResponseEntity<SuccessResponse<TodayStatisticResponse>> getTodayStatistics();

    @ApiResponse(
            responseCode = "404",
            description = """
        - POSTING_NOT_FOUND : 존재하지 않는 공고입니다.
        - STAGE_NOT_FOUND : 존재하지 않는 전형입니다.
        """,
            content = @Content(
                    schema = @Schema(implementation = FailureResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = """
        - STAGE_NOT_IN_POSTING : 해당 공고의 전형 단계가 아닙니다.
        """,
            content = @Content(
                    schema = @Schema(implementation = FailureResponse.class)
            )
    )
    @Operation(summary= "공고 상세 - 공고 전형별 집계 조회",
            description = """
                    공고 전형별 집계 조회합니다. 
                    - 공고 전형 별 passCount,failCount,pendingCount를 제시합니다.
                    """
    )
    ResponseEntity<SuccessResponse<PostingStageStatisticResponse>> getStagesStatistic(
            @PathVariable Long postingId,
            @PathVariable Long stageId
    );
}