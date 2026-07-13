package org.sopt.haphap.domain.search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import org.sopt.haphap.domain.posting.dto.response.PopularPostingListResponse;
import org.sopt.haphap.domain.search.dto.AutocompleteResponse;
import org.sopt.haphap.domain.search.dto.SearchPostingListResponse;
import org.sopt.haphap.global.dto.FailureResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "검색", description = "검색 관련 API 입니다")
public interface SearchApiDocs {

    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = PopularPostingListResponse.class),
                    examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "code": "POPULAR_POSTINGS_FETCHED",
                              "message": "인기 공고 목록 조회에 성공했습니다.",
                              "data": {
                                "postings": [
                                  {
                                    "id": 12,
                                    "title": "2026 상반기 신입 공채",
                                    "companyName": "토스",
                                    "category": "개발",
                                    "nextStage": "1차 면접",
                                    "daysUntilNextStage": 3,
                                    "imageUrl": "https://.../toss.png"
                                  }
                                ]
                              }
                            }
                            """)))
    @Operation(summary = "인기 공고 목록 조회",
            description = """
                    검색 진입화면에 노출할 인기 공고 상위 4개를 반환합니다.
                    카드 클릭 + 상세진입 합산 조회수 기준이며, 매시 정각에 갱신되는 캐시를 서빙합니다
                    (최대 1시간 지연 가능). 아직 집계된 데이터가 없으면 빈 배열을 반환합니다.
                    """)
    ResponseEntity<SuccessResponse<PopularPostingListResponse>> getPopularPostings();

    @ApiResponse(responseCode = "200", description = "자동완성 결과",
            content = @Content(schema = @Schema(implementation = AutocompleteResponse.class),
                    examples = @ExampleObject(value = """
                        {
                          "status": 200,
                          "code": "AUTOCOMPLETE_FETCHED",
                          "message": "검색 자동완성 조회에 성공했습니다.",
                          "data": {
                            "relatedPostings": [
                              {"postingId":1,"name":"카카오 기획 공개채용","imageUrl":"https://.../kakao.png","highlightRanges":[{"start":0,"end":3}]}
                            ],
                            "relatedKeywords": [
                              {"keywordId":7,"name":"카카오 스타일","highlightRanges":[{"start":0,"end":3}]}
                            ]
                          }
                        }
                        """)))
    @Operation(summary = "검색 자동완성",
            description = """
                입력한 키워드로 "기업명 + 공고명" 결합 문자열 및 관련 검색어를 매칭해 자동완성 결과를 반환합니다.
                relatedPostings: 기업명+공고명이 매칭된 공고 바로가기 목록 (postingId, imageUrl로 상세 이동/로고 표시).
                    마감된 공고는 제외하며, 검색어와 완전히 일치하는 항목을 최상단에 노출하고 나머지는 가나다순 정렬합니다. 최대 5개.
                    imageUrl은 해당 기업에 AUTOCOMPLETE 타입 이미지가 등록되어 있지 않으면 null일 수 있습니다.
                relatedKeywords: 관련 검색어 목록 (keywordId로 GET /api/v1/search/postings?relatedKeywordId= 호출해 필터링된 목록으로 이동).
                    검색어와 완전히 일치하는 항목을 최상단에 노출하고 나머지는 가나다순 정렬합니다. 최대 10개.
                highlightRanges는 relatedPostings의 경우 "기업명+공고명" 결합 문자열, relatedKeywords의 경우 관련 검색어 텍스트를 기준으로
                    매칭된 텍스트의 시작(inclusive)/끝(exclusive) offset입니다.
                결과가 0건이어도 에러가 아니라 빈 배열로 응답합니다.
                """)
    ResponseEntity<SuccessResponse<AutocompleteResponse>> autocomplete(
            @Parameter(description = "검색 키워드. 공백이거나 완성되지 않은 한글 자모(예: \"ㄱㄴㄷ\")만 입력하면 빈 배열 응답") String q);

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 결과",
                    content = @Content(schema = @Schema(implementation = SearchPostingListResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "POSTING_SEARCH_FETCHED",
                                      "message": "검색 결과 공고 목록 조회에 성공했습니다.",
                                      "data": {
                                        "postings": [
                                          {"postingId":1,"companyName":"카카오","title":"백엔드 개발자","categoryName":"개발","nextStage":"1차 면접","imageUrl":"https://...","dDay":3}
                                        ],
                                        "page": 0,
                                        "size": 20,
                                        "hasNext": true
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "검색어 미입력 (q, relatedKeywordId 둘 다 없음)",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 400, "code": "KEYWORD_REQUIRED", "message": "검색어 입력은 필수입니다." }
                                    """))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리 또는 관련 검색어",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = {
                                    @ExampleObject(name = "카테고리 없음", value = """
                                            { "status": 404, "code": "CATEGORY_NOT_FOUND", "message": "존재하지 않는 카테고리입니다." }
                                            """),
                                    @ExampleObject(name = "관련 검색어 없음", value = """
                                            { "status": 404, "code": "RELATED_KEYWORD_NOT_FOUND", "message": "존재하지 않는 관련 검색어입니다." }
                                            """)
                            }))
    })
    @Operation(summary = "검색 결과 공고 목록 조회",
            description = """
                    검색어 확정 후 결과 화면에 노출할 공고 목록을 반환합니다.
                    q는 직접 입력한 텍스트, relatedKeywordId는 자동완성 관련 검색어를 탭했을 때 사용합니다.
                    둘 다 오면 relatedKeywordId가 우선하고 q는 무시됩니다. 둘 다 없으면 400(KEYWORD_REQUIRED)을 반환합니다.
                    relatedKeywordId가 존재하지 않거나 비활성화된 값이면 404(RELATED_KEYWORD_NOT_FOUND)를 반환합니다.
                    category는 콤마로 구분해 복수 카테고리를 전달할 수 있고, 존재하지 않는 카테고리면 404(CATEGORY_NOT_FOUND)를 반환합니다
                    ('전체' 선택 시 파라미터 생략).
                    정렬 기준은 다음 전형 발표 예상일이 가까운 순이며, page/size 기반 페이지네이션입니다.
                    """)
    ResponseEntity<SuccessResponse<SearchPostingListResponse>> searchPostings(
            @Parameter(description = "검색 키워드. 공백이거나 완성되지 않은 한글 자모(예: \"ㄱㄴㄷ\")만 입력하면 빈 배열 응답") String q,
            @Parameter(description = "관련 검색어 id (자동완성 keyword 탭 시). q보다 우선함") Long relatedKeywordId,
            @Parameter(description = "카테고리 필터, 콤마로 구분해 복수 전달 가능. 전체 조회 시 파라미터 생략") String category,
            @Parameter(description = "페이지 번호, 0부터 시작, 기본 0") Integer page,
            @Parameter(description = "페이지 크기, 기본 20, 최대 50") Integer size);
}