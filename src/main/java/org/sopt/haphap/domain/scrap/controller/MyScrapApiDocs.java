package org.sopt.haphap.domain.scrap.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.haphap.domain.scrap.dto.response.MyScrapListResponse;
import org.sopt.haphap.global.dto.FailureResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "마이페이지", description = "마이페이지 화면 관련 API 입니다")
public interface MyScrapApiDocs {

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MyScrapListResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "MY_SCRAPS_FETCHED",
                                      "message": "찜한 공고 목록 조회에 성공했습니다.",
                                      "data": {
                                        "totalCount": 2,
                                        "scraps": [
                                          {
                                            "postingId": 55,
                                            "companyName": "카카오",
                                            "category": "개발/데이터",
                                            "title": "2026 신입 개발자 공개채용",
                                            "logoImageUrl": "https://example.com/logo.png"
                                          },
                                          {
                                            "postingId": 54,
                                            "companyName": "카카오",
                                            "category": "개발/데이터",
                                            "title": "2026 신입 개발자 공개채용",
                                            "logoImageUrl": "https://example.com/logo.png"
                                          }
                                        ]
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음/만료/유효하지 않음)",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 401, "code": "UNAUTHORIZED", "message": "인증이 필요합니다." }
                                    """)))
    })
    @Operation(summary = "[찜한 공고] 목록 조회",
            description = """
                    내가 찜한 공고 전체를 반환합니다. (페이지네이션 없음)
                    - 정렬: 찜한 시각 최신순, 같은 시각이면 공고 ID 내림차순
                    - 찜을 취소했다가 다시 찜한 경우 다시 찜한 시각 기준입니다.
                    - 마감된 공고도 포함합니다.
                    - totalCount: 찜한 공고 수 (= scraps 길이)
                    - 찜한 공고가 없으면 totalCount 0, 빈 배열을 반환합니다.
                    """)
    ResponseEntity<SuccessResponse<MyScrapListResponse>> getMyScraps(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId);
}
