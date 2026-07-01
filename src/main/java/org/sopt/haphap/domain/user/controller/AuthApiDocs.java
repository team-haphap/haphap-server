package org.sopt.haphap.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.sopt.haphap.domain.user.dto.KakaoLoginRequest;
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
                    - 신규 사용자의 경우 isNew: true가 반환됩니다.
                    - 기존 사용자의 경우 isNew: false가 반환됩니다.
                    """)
    ResponseEntity<SuccessResponse<AuthResponse>> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request);

    @Operation(summary = "토큰 재발급",
            description = """
                    리프레시 토큰으로 액세스 토큰을 재발급합니다.
                    - Authorization 헤더에 Bearer {refreshToken}을 넣어주세요.
                    - 만료된 리프레시 토큰으로 요청 시 401 에러가 반환됩니다.
                    """)
    ResponseEntity<SuccessResponse<AuthResponse>> reissue(@RequestHeader("Authorization") String authorization);

    @Operation(summary = "로그아웃",
            description = """
                    액세스 토큰을 블랙리스트 처리하여 로그아웃합니다.
                    - Authorization 헤더에 Bearer {accessToken}을 넣어주세요.
                    - 로그아웃 성공 시 204 No Content가 반환됩니다.
                    """)
    ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization);
}