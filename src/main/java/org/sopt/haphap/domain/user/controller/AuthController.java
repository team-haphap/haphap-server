package org.sopt.haphap.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.user.dto.AuthResponse;
import org.sopt.haphap.domain.user.dto.KakaoLoginRequest;
import org.sopt.haphap.global.code.AuthSuccessCode;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.sopt.haphap.domain.user.service.AuthService;
import org.sopt.haphap.global.code.GlobalErrorCode;
import org.sopt.haphap.global.exception.CustomException;
import org.sopt.haphap.global.jwt.BearerTokenExtractor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApiDocs {

    private final AuthService authService;

    @PostMapping("/kakao")
    public ResponseEntity<SuccessResponse<AuthResponse>> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request) {
        return ResponseEntity.ok(SuccessResponse.of(AuthSuccessCode.KAKAO_LOGIN_SUCCESS, authService.kakaoLogin(request.accessToken())));
    }

    @PostMapping("/reissue")
    public ResponseEntity<SuccessResponse<AuthResponse>> reissue(@RequestHeader("Authorization") String authorization) {
        String token = BearerTokenExtractor.extract(authorization);
        if (token == null) { throw new CustomException(GlobalErrorCode.BAD_REQUEST); }
        return ResponseEntity.ok(SuccessResponse.of(AuthSuccessCode.REISSUE_SUCCESS, authService.reissue(token)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization) {
        String token = BearerTokenExtractor.extract(authorization);
        if (token == null) { throw new CustomException(GlobalErrorCode.BAD_REQUEST); }
        authService.logout(token);
        return ResponseEntity.noContent().build();
    }
}