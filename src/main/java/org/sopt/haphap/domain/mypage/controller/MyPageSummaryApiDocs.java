package org.sopt.haphap.domain.mypage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.haphap.domain.mypage.dto.response.MyPageSummaryResponse;
import org.sopt.haphap.global.dto.FailureResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "마이페이지", description = "마이페이지 화면 관련 API 입니다")
public interface MyPageSummaryApiDocs {

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MyPageSummaryResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "MY_PAGE_SUMMARY_FETCHED",
                                      "message": "마이페이지 요약 정보 조회에 성공했습니다.",
                                      "data": {
                                        "member": {
                                          "memberId": 12,
                                          "nickname": "익명의 조르디",
                                          "profileImageUrl": "https://example.com/profile.png",
                                          "point": 300
                                        },
                                        "passCertification": {
                                          "rejectedCount": 2,
                                          "approvedCount": 0
                                        },
                                        "hasUpcomingAnnouncement": true
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음/만료/유효하지 않음)",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 401, "code": "UNAUTHORIZED", "message": "인증이 필요합니다." }
                                    """))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 404, "code": "USER_NOT_FOUND", "message": "존재하지 않는 유저입니다." }
                                    """)))
    })
    @Operation(summary = "[마이페이지] 요약 정보 조회",
            description = """
                    마이페이지 상단에 노출할 회원 정보, 합격 인증 건수, 이번 주 발표 예상 여부를 반환합니다.
                    - member.nickname: 익명 닉네임 (예: 익명의 조르디)
                    - member.profileImageUrl: 프로필 이미지가 없으면 null
                    - passCertification: 내가 합격으로 등록한 결과 중 인증 승인/반려 건수. 승인 대기 중인 건은 포함하지 않습니다.
                    - hasUpcomingAnnouncement: 이번 주(월~일, 이미 지난 요일 포함)에 발표가 예상되는 전형이 있는 공고가 하나라도 있으면 true (전체 공고 기준)
                    """)
    ResponseEntity<SuccessResponse<MyPageSummaryResponse>> getSummary(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId);
}
