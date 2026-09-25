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
                    (1) 카카오/애플 연동 해제 -> 개인정보 파기 및 개인 데이터 (알림, 알림 설정, 푸시 토큰) 삭제 -> 토큰 폐기
                    (2) 합불 기록은 작성자를 알 수 없는 비식별 상태로 전체 집계에만 남도록
                    (3) 탈퇴 사유는 필수적이고, ETC 선택 시 etcReason 이 필요합니다 (공백제외 1-200자)
                    (4) 외부 연동 해제에 실패하면 503 반환하고, 어떤 데이터도 변경되지 않습니다. 
                    (5) Authorization 헤더에 Bearer {accessToken}을 넣어주세요.
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
            @ApiResponse(responseCode = "204", description = "탈퇴 성공 (응답 본문 없음)"),
            @ApiResponse(responseCode = "400", description = "탈퇴 사유 누락 / 기타 사유 공백 / 200자 초과",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 400, "code": "INVALID_INPUT_VALUE", "message": "기타 사유를 입력해주세요." }
                                    """))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음/만료/유효하지 않음)",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 401, "code": "UNAUTHORIZED", "message": "인증이 필요합니다." }
                                    """))),
            @ApiResponse(responseCode = "404", description = "존재하지 않거나 이미 탈퇴한 회원",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 404, "code": "USER_NOT_FOUND", "message": "존재하지 않는 유저입니다." }
                                    """))),
            @ApiResponse(responseCode = "503", description = "카카오/애플 연동 해제 실패 (재시도 필요)",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = @ExampleObject(value = """
                                    { "status": 503, "code": "KAKAO_SERVER_UNAVAILABLE", "message": "카카오 서버 응답이 원활하지 않습니다. 잠시 후 다시 시도해주세요." }
                                    """)))
    })
    ResponseEntity<Void> withdraw(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorization,  // ← hidden 추가
            @Valid @RequestBody WithdrawRequest request);
}