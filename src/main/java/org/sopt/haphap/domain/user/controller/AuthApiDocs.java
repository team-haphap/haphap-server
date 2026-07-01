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
import org.sopt.haphap.domain.user.dto.AgreementSubmitRequest;

@Tag(name = "인증", description = "카카오 소셜 로그인 및 토큰 관리를 위한 API")
public interface AuthApiDocs {

    @Operation(summary = "카카오 소셜 로그인",
            description = """
                    카카오 액세스 토큰으로 로그인합니다.
                    - 카카오에서 발급받은 액세스 토큰을 requestBody에 넣어주세요.
                    - 신규 사용자의 경우 isNewUser: true가 반환됩니다.
                    - 기존 사용자의 경우 isNewUser: false가 반환됩니다.
                    """)
    ResponseEntity<SuccessResponse<AuthResponse>> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request);

    @Operation(summary = "토큰 재발급",
            description = """
                    리프레시 토큰으로 액세스 토큰을 재발급합니다.
                    - Authorization 헤더에 Bearer {refreshToken}을 넣어주세요.
                    - 만료된 리프레시 토큰으로 요청 시 401 에러가 반환됩니다.
                    """)
    ResponseEntity<SuccessResponse<AuthResponse>> reissue(@RequestHeader("Authorization") String authorization);

    @Operation(summary = "약관 동의 및 가입 완료",
            description = """
                    신규 유저가 약관 동의를 제출하여 가입을 완료합니다.
                    - Authorization 헤더에 Bearer {signupToken}을 넣어주세요 (카카오 로그인 응답의 signupToken 값).
                    - 필수 약관(개인정보 수집·이용, 위치정보 이용약관, 만 14세 이상 확인) 중 하나라도 미동의면 400 에러가 반환됩니다.
                    - 성공 시 정식 accessToken/refreshToken이 발급됩니다.
                    """)
    ResponseEntity<SuccessResponse<AuthResponse>> submitAgreements(@RequestHeader("Authorization") String authorization, @Valid @RequestBody AgreementSubmitRequest request);
    @Operation(summary = "로그아웃",
            description = """
                    액세스 토큰을 블랙리스트 처리하여 로그아웃합니다.
                    - Authorization 헤더에 Bearer {accessToken}을 넣어주세요.
                    - 로그아웃 성공 시 204 No Content가 반환됩니다.
                    """)
    ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization);
}