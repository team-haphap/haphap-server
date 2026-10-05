package org.sopt.haphap.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.sopt.haphap.domain.user.dto.MemberResponse;
import org.sopt.haphap.domain.user.dto.WithdrawRequest;
import org.sopt.haphap.global.dto.FailureResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "회원", description = "마이페이지 등 회원 정보 조회를 위한 API")
public interface MemberApiDocs {

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MemberResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "MEMBER_INFO_FETCHED",
                                      "message": "사용자 정보 조회에 성공했습니다.",
                                      "data": {
                                        "name": "김소프트",
                                        "anonymousName": "익명의 판다",
                                        "email": "user@example.com",
                                        "profileImageUrl": "https://.../profile.png",
                                        "provider": "KAKAO"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음/만료/유효하지 않음)",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 401, "code": "UNAUTHORIZED", "message": "인증이 필요합니다." }
                                    """)))
    })
    @Operation(summary = "사용자 정보 조회",
            description = """
                    마이페이지에 필요한 사용자 정보를 조회합니다.
                    - Authorization 헤더에 Bearer {accessToken}을 넣어주세요. (로그인한 본인 정보만 조회됩니다)
                    """)
    ResponseEntity<SuccessResponse<MemberResponse>> getMyInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId);

    @Operation(summary = "회원 탈퇴",
            description = """
                    본인 계정을 즉시 탈퇴 처리합니다. 
                    (1) 요청 즉시: 개인정보 파기, 알림·알림 설정·푸시 토큰·조회 이력 삭제, 합불 기록 비식별화(전체 집계는 유지),
                        합격 인증 이미지 삭제(약 25시간 이내), 액세스/리프레시 토큰 폐기
                    (2) 카카오/애플 연동 해제는 응답 이후 진행되며, 실패 시 서버가 자동 재시도합니다. 연동 해제 결과와 무관하게 204를 반환합니다.
                    (3) 탈퇴 사유 필수. ETC 선택 시 etcReason 필요 (앞쪽 공백 제외 1~150자, 이모지 1개 = 1자, 공백만 입력 불가)
                    (4) 동시에 들어온 중복 요청도 204를 반환합니다.
                        단, 탈퇴 완료 후 같은 토큰으로 재요청하면 401이 반환되니 '탈퇴 완료'로 간주하고 로그인 화면으로 이동해주세요.
                    (5) 처리 중 서버 오류(5xx)가 나면 어떤 데이터도 변경되지 않습니다. 재시도해주세요.
                    (6) 탈퇴 직후 연동 해제가 끝나기 전에 같은 소셜 계정으로 로그인하면 403 WITHDRAWAL_IN_PROGRESS가 반환됩니다.
                    (7) Authorization 헤더에 Bearer {accessToken}을 넣어주세요.
                    """)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(schema = @Schema(implementation = WithdrawRequest.class),
                    examples = {
                            @ExampleObject(name = "일반 사유", value = """
                                    { "reason": "LOW_USAGE" }
                                    """),
                            @ExampleObject(name = "기타 사유", value = """
                                    { "reason": "ETC", "etcReason": "알림이 너무 많이 와요" }
                                    """)
                    }))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "탈퇴 성공 / 이미 처리된 중복 요청 (응답 본문 없음)"),
            @ApiResponse(responseCode = "400", description = "탈퇴 사유 누락 / 기타 사유 공백 / 150자 초과",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 400, "code": "INVALID_INPUT_VALUE", "message": "기타 사유를 150자 이하로 입력해주세요." }
                                    """))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음/만료/유효하지 않음, 또는 이미 탈퇴 완료된 토큰)",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 401, "code": "UNAUTHORIZED", "message": "인증이 필요합니다." }
                                    """))),
            @ApiResponse(responseCode = "404", description = "존재하지 않거나 이미 탈퇴한 회원",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 404, "code": "USER_NOT_FOUND", "message": "존재하지 않는 유저입니다." }
                                    """)))
    })
    ResponseEntity<Void> withdraw(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorization,  // ← hidden 추가
            @Valid @RequestBody WithdrawRequest request);
}