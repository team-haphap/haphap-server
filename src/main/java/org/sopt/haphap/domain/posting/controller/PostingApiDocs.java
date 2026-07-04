package org.sopt.haphap.domain.posting.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.haphap.domain.posting.dto.PostingListResponse;
import org.sopt.haphap.domain.posting.dto.PostingStageListResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "공고",description = "공고관련 API 입니다")
public interface PostingApiDocs {
    @Operation(summary = "공고명 리스트 조회 " ,description = "전체 공고명을 가나다 순으로 반환합니다.")
    ResponseEntity<SuccessResponse<PostingListResponse>> getPostings();

    @Operation(summary = "공고 별 전형 조회",description = "해당 공고의 전형을 반환합니다.")
    ResponseEntity<SuccessResponse<PostingStageListResponse>> getStages(@PathVariable Long postingId);

    @Operation(summary = "공고 상세 조회 기록",
            description = "상세 페이지 진입 시 호출합니다. 인기 공고 집계에 사용되며 응답 본문은 없습니다.")
    ResponseEntity<Void> recordView(@PathVariable Long postingId);

    @Operation(summary = "공고 카드 클릭 기록",
            description = """
                    홈/리스트/인기 공고 섹션 등 공고 카드가 노출되는 모든 화면에서 카드를 클릭했을 때 호출합니다.
                    상세 페이지 진입 기록과 합산되어 인기 공고 집계(카드 클릭률 + 상세페이지 진입자 수)에 사용되며,
                    응답 본문은 없습니다.
                    """)
    ResponseEntity<Void> recordCardClick(@PathVariable Long postingId);
}
