package org.sopt.haphap.domain.mypage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.haphap.domain.mypage.dto.request.MyRegistrationFilter;
import org.sopt.haphap.domain.mypage.dto.response.MyRegistrationListResponse;
import org.sopt.haphap.domain.mypage.dto.response.MyRegistrationPreviewListResponse;
import org.sopt.haphap.global.dto.FailureResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "마이페이지", description = "마이페이지 화면 관련 API 입니다")
public interface MyPageApiDocs {

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MyRegistrationPreviewListResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "MY_REGISTRATIONS_PREVIEW_FETCHED",
                                      "message": "마이페이지 지원 내역 조회에 성공했습니다.",
                                      "data": {
                                        "registrations": [
                                          {
                                            "postingId": 54,
                                            "title": "2026 신입 개발자 공개채용",
                                            "currentState": "서류",
                                            "applicantCount": 20,
                                            "logoImageUrl": "https://example.com/logo.png",
                                            "appliedAt": "2026-11-01T14:30:00"
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
    @Operation(summary = "[내 지원 내역] 미리보기 조회",
            description = """
                    결과를 등록한 공고를 최근 등록/수정 순으로 최대 10건 반환합니다. 마감된 공고도 포함합니다.
                    - 합격은 운영진의 합격 인증 승인이 나야 등록한 것으로 취급합니다. (승인 전/반려된 합격은 제외)
                    - appliedAt: 이 공고에 마지막으로 결과를 등록/수정한 시각 (합격 승인 시각 포함)
                    - currentState: 전체보기와 같은 현재 전형. 현재 전형명, 마감된 공고는 "마감", 전형 시작 전이면 "진행 예정"
                    - applicantCount: 공고 상세의 참여 인원과 같은 기준
                    - 등록한 공고가 없으면 빈 배열을 반환합니다. (영역 미노출은 프론트에서 처리)
                    """)
    ResponseEntity<SuccessResponse<MyRegistrationPreviewListResponse>> getRegistrationPreview(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId);

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MyRegistrationListResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "MY_REGISTRATIONS_FETCHED",
                                      "message": "내 지원 내역 조회에 성공했습니다.",
                                      "data": {
                                        "counts": {
                                          "totalCount": 24,
                                          "inProgressCount": 12,
                                          "allClosedCount": 12
                                        },
                                        "registrations": [
                                          {
                                            "postingId": 54,
                                            "companyName": "카카오",
                                            "title": "2026 신입 개발자 공개채용",
                                            "logoImageUrl": "https://example.com/logo.png",
                                            "currentState": "1차 면접",
                                            "myResult": "서류 합격"
                                          }
                                        ]
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "filter 값이 ALL / IN_PROGRESS / ALL_CLOSED 가 아님",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 400, "code": "METHOD_ARGUMENT_TYPE_MISMATCH", "message": "요청 파라미터 타입이 올바르지 않습니다." }
                                    """))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음/만료/유효하지 않음)",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 401, "code": "UNAUTHORIZED", "message": "인증이 필요합니다." }
                                    """)))
    })
    @Operation(summary = "[내 지원 내역] 전체보기 조회",
            description = """
                    결과를 등록한 공고 전부를 최근 등록/수정 순으로 반환합니다.
                    - filter: ALL(기본) / IN_PROGRESS(전형 진행중) / ALL_CLOSED(모든 전형 마감)
                    - counts: 탭에 표시할 개수. 선택한 filter와 무관하게 항상 전체 기준입니다.
                    - 합격은 운영진의 합격 인증 승인이 나야 등록한 것으로 취급합니다. (승인 전/반려된 합격은 제외)
                    - currentState: 공고 상세의 현재 전형과 같은 기준. 현재 전형명, 마감된 공고는 "마감", 전형 시작 전이면 "진행 예정"
                    - myResult: 내가 등록한 결과 중 전형 순서상 가장 뒤 전형의 결과 ("{전형명} 합격/불합격/대기").
                      등록/수정 시점과 무관하게 공고에 설정된 전형 순서 기준입니다.
                    """)
    ResponseEntity<SuccessResponse<MyRegistrationListResponse>> getRegistrations(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "ALL / IN_PROGRESS / ALL_CLOSED (기본값 ALL)") MyRegistrationFilter filter);
}
