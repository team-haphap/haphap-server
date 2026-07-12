package org.sopt.haphap.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.sopt.haphap.domain.user.dto.KakaoLoginRequest;
import org.sopt.haphap.global.dto.FailureResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.sopt.haphap.domain.user.dto.AuthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "인증", description = "카카오 소셜 로그인 및 토큰 관리를 위한 API")
public interface AuthApiDocs {

    @Operation(summary = "카카오 소셜 로그인",
            description = """
                    카카오 액세스 토큰으로 로그인합니다.
                    - 카카오에서 발급받은 액세스 토큰을 requestBody에 넣어주세요.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(value = """
                            {
                              "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                              "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
                              "name": "김소프트",
                              "anonymousName": "익명의 판다",
                              "profileImageUrl": "https://.../profile.png"
                            }
                            """))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class),
                            examples = {
                                    @ExampleObject(name = "카카오 계정 정보를 가져올 수 없음", value = """
                                    {
                                      "status": 400,
                                      "code": "KAKAO_ACCOUNT_NOT_FOUND",
                                      "message": "카카오 계정 정보를 가져올 수 없습니다."
                                    }
                                    """),
                                    @ExampleObject(name = "요청값 검증 실패 (accessToken 누락)", value = """
                                    {
                                      "status": 400,
                                      "code": "INVALID_INPUT_VALUE",
                                      "message": "must not be blank"
                                    }
                                    """)
                            })),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 카카오 액세스 토큰",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class))),
            @ApiResponse(responseCode = "503", description = "카카오 서버 응답 지연/오류",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class)))
    })
    ResponseEntity<SuccessResponse<AuthResponse>> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request);

    @Operation(summary = "토큰 재발급",
            description = """
                    리프레시 토큰으로 액세스 토큰을 재발급합니다.
                    - Authorization 헤더에 Bearer {refreshToken}을 넣어주세요.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(value = """
                            {
                              "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                              "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
                              "name": "김소프트",
                              "anonymousName": "익명의 판다",
                              "profileImageUrl": "https://.../profile.png"
                            }
                            """))),
            @ApiResponse(responseCode = "401", description = "유효하지 않거나 저장된 값과 불일치하는 리프레시 토큰",
                    content = @Content(schema = @Schema(implementation = FailureResponse.class)))
    })
    ResponseEntity<SuccessResponse<AuthResponse>> reissue(@RequestHeader("Authorization") String authorization);

    @Operation(summary = "로그아웃",
            description = """
                    액세스 토큰을 블랙리스트 처리하여 로그아웃합니다.
                    - Authorization 헤더에 Bearer {accessToken}을 넣어주세요.
                    """)
    @ApiResponse(responseCode = "204", description = "로그아웃 성공")
    ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization);
}