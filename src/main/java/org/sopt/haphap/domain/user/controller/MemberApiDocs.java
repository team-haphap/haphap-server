package org.sopt.haphap.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.sopt.haphap.domain.user.dto.MemberResponse;
import org.sopt.haphap.global.dto.FailureResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

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
                                "profileImageUrl": "https://.../profile.png"
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
}