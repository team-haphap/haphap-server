package org.sopt.haphap.domain.search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.sopt.haphap.domain.posting.dto.response.PopularPostingListResponse;
import org.sopt.haphap.domain.search.dto.AutocompleteResponse;
import org.sopt.haphap.domain.search.dto.SearchPostingListResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "검색", description = "검색 관련 API 입니다")
public interface SearchApiDocs {

    @Operation(summary = "인기 공고 목록 조회",
            description = """
                    검색 진입화면에 노출할 인기 공고 상위 4개를 반환합니다.
                    카트클릭 + 상세진입 합산 기준이며, 매 요청마다 실시간 계산됩니다
                    아직 집계된 데이터가 없으면 빈 배열을 반환합니다.
                    """)
    ResponseEntity<SuccessResponse<PopularPostingListResponse>> getPopularPostings();

    @Operation(summary = "검색 자동완성",
            description = """
                    입력한 키워드로 공고명을 매칭해 자동완성 결과를 반환합니다.
                    type: company(기업 바로가기) / keyword(관련 검색어)로 구분되며, 각 항목의
                    highlightRanges는 매칭된 텍스트의 시작/끝 offset입니다.
                    결과가 0건이어도 에러가 아니라 빈 배열로 응답합니다.
                    """)
    @ApiResponse(responseCode = "200", description = "자동완성 결과",
            content = @Content(examples = @ExampleObject(value = """
                    { "results": [
                        {"type":"company","name":"카카오","highlightRanges":[{"start":0,"end":2}],"postingId":1},
                        {"type":"keyword","name":"백엔드 개발자","highlightRanges":[{"start":3,"end":5}],"postingId":null}
                    ] }
                    """)))
    ResponseEntity<SuccessResponse<AutocompleteResponse>> autocomplete(
            @Parameter(description = "검색 키워드, 최소 1글자") String q);

    @Operation(summary = "검색 결과 공고 목록 조회",
            description = """
                검색어 확정 후 결과 화면에 노출할 공고 목록을 반환합니다.
                q는 공고명 포함된 경우 매칭되며, category로 복수 카테고리 필터링,
                status(open/closed)로 모집 상태 필터링이 가능합니다.
                정렬 기준은 다음 전형 발표 예상일이 가까운 순이며, page/size 기반 페이지네이션입니다.
                """)
    @ApiResponse(responseCode = "200", description = "검색 결과",
            content = @Content(examples = @ExampleObject(value = """
                    { "postings": [
                        {"postingId":1,"companyName":"카카오","title":"백엔드 개발자","categoryName":"개발","dDay":3,"status":"open"}
                      ], "page": 0, "size": 20, "hasNext": true }
                    """)))
    ResponseEntity<SuccessResponse<SearchPostingListResponse>> searchPostings(
            @Parameter(description = "검색 키워드") String q,
            @Parameter(description = "카테고리 필터 (복수 선택 가능)") List<String> category,
            @Parameter(description = "모집 상태 필터: open 또는 closed") String status,
            @Parameter(description = "페이지 번호, 0부터 시작, 기본 0") Integer page,
            @Parameter(description = "페이지 크기, 기본 20, 최대 50") Integer size);
}