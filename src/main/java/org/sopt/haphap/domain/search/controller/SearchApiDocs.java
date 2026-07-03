package org.sopt.haphap.domain.search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.haphap.domain.search.dto.PopularSearchPostingListResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "검색", description = "검색 관련 API 입니다")
public interface SearchApiDocs {

    @Operation(summary = "인기 공고 목록 조회",
            description = """
                    검색 진입화면에 노출할 인기 공고 상위 4개를 반환합니다.
                    최근 상세페이지 조회수 기준이며 10분 주기로 캐시가 갱신됩니다.
                    아직 집계된 데이터가 없으면 빈 배열을 반환합니다.
                    """)
    ResponseEntity<SuccessResponse<PopularSearchPostingListResponse>> getPopularPostings();
}