package org.sopt.haphap.domain.home.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.sopt.haphap.domain.home.dto.response.MyApplicationListResponse;
import org.sopt.haphap.domain.home.dto.response.RecentViewListResponse;
import org.sopt.haphap.global.dto.FailureResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "홈", description = "홈 화면 개인화 섹션 관련 API 입니다")
public interface HomeApiDocs {

    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = MyApplicationListResponse.class),
                    examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "code": "MY_APPLICATIONS_FETCHED",
                              "message": "내 지원 공고 조회에 성공했습니다.",
                              "data": {
                                "applications": [
                                  {
                                    "postingId": 12,
                                    "companyName": "토스",
                                    "title": "2026 상반기 신입 공채",
                                    "category": "개발/데이터",
                                    "currentStageStatus": "1차 면접 발표 중",
                                    "dDayLabel": "D-3",
                                    "logoImageUrl": "https://.../toss.png"
                                  }
                                ]
                              }
                            }
                            """)))
    @Operation(summary = "[내 지원] 조회",
            description = """
                    최근 활동(등록/합격 승인) 순으로 최대 3건을 반환합니다. 마감된 공고는 제외합니다.
                    합격을 등록했지만 운영진 승인이 아직 안 된 경우는 등록한 결과가 없는 것으로 취급합니다.
                    dDayLabel은 현재 진행 중인 전형이 아니라 다음 전형 발표 예상일 기준입니다.
                    """)
    ResponseEntity<SuccessResponse<MyApplicationListResponse>> getMyApplications(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId);

    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = RecentViewListResponse.class),
                    examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "code": "RECENT_VIEWS_FETCHED",
                              "message": "최근 조회한 공고 조회에 성공했습니다.",
                              "data": {
                                "views": [
                                  {
                                    "postingId": 12,
                                    "companyName": "토스",
                                    "title": "2026 상반기 신입 공채",
                                    "category": "개발/데이터",
                                    "nextStage": "1차 면접",
                                    "dDayLabel": "D-3",
                                    "logoImageUrl": "https://.../toss.png"
                                  }
                                ]
                              }
                            }
                            """)))
    @Operation(summary = "[최근 조회한 공고] 조회",
            description = """
                    최근 조회한 순으로 최대 10건을 반환합니다(30일 이내 조회분만). 마감된 공고도 노출하되,
                    이 경우 nextStage는 null, dDayLabel은 "마감"으로 내려갑니다.
                    dDayLabel은 현재 진행 중인 전형이 아니라 다음 전형 발표 예상일 기준입니다.
                    """)
    ResponseEntity<SuccessResponse<RecentViewListResponse>> getRecentViews(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId);

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = RecentViewListResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "POPULAR_POSTINGS_FETCHED",
                                      "message": "지금 많이 보는 공고 조회에 성공했습니다.",
                                      "data": {
                                        "views": [
                                          {
                                            "postingId": 12,
                                            "companyName": "토스",
                                            "title": "2026 상반기 신입 공채",
                                            "category": "개발/데이터"add,
                                            "nextStage": "1차 면접",
                                            "dDayLabel": "D-3",
                                            "logoImageUrl": "https://.../toss.png"
                                          }
                                        ]
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 404, "code": "CATEGORY_NOT_FOUND", "message": "존재하지 않는 카테고리입니다." }
                                    """)))
    })
    @Operation(summary = "[지금 많이 보는 공고] 조회",
            description = """
                    정각 기준 직전 59분간의 조회수(상세 진입 기준) 상위 공고를 최대 10건 반환합니다.
                    category는 콤마로 구분해 복수 선택 가능하며, 생략하면 [전체]로 간주합니다.
                    조회수 동점이면 공고명 가나다순(숫자 → 한글 → 영문 → 그 외)으로 정렬합니다.
                    마감된 공고도 노출 대상이며, 이 경우 nextStage는 null, dDayLabel은 "마감"으로 내려갑니다.
                    직전 시간대에 조회 데이터가 전혀 없으면 직전에 갱신된 목록을 그대로 유지합니다(빈 상태 노출 방지).
                    """)
    ResponseEntity<SuccessResponse<RecentViewListResponse>> getPopularPostings(
            @Parameter(description = "카테고리 필터, 복수 전달 가능. 전체 조회 시 파라미터 생략") List<String> category);
}
