package org.sopt.haphap.domain.mypage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
                                            "currentStageStatus": "서류 발표 예상",
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
                    - 합격을 등록했지만 운영진 승인이 아직 안 된 경우는 등록한 결과가 없는 것으로 취급합니다.
                    - appliedAt: 이 공고에 마지막으로 결과를 등록/수정한 시각 (합격 승인 시각 포함)
                    - currentStageStatus: 다음 발표 예상 전형. 현재 전형 바로 다음 전형의 "{전형명} 발표 예상"
                      (현재 전형이 아직 없으면 첫 전형). 마감된 공고는 "마감", 최종 전형이라 다음 전형이 없으면 null
                    - applicantCount: 공고 상세의 참여 인원과 같은 기준
                    - 등록한 공고가 없으면 빈 배열을 반환합니다. (영역 미노출은 프론트에서 처리)
                    """)
    ResponseEntity<SuccessResponse<MyRegistrationPreviewListResponse>> getRegistrationPreview(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId);
}
