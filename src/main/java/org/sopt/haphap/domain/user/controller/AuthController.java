package org.sopt.haphap.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.user.dto.AuthResponse;
import org.sopt.haphap.domain.user.dto.KakaoLoginRequest;
import org.sopt.haphap.domain.user.service.AuthService;
import org.sopt.haphap.global.code.GlobalErrorCode;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/kakao")
    public ResponseEntity<AuthResponse> kakaoLogin(@RequestBody KakaoLoginRequest request) {
        return ResponseEntity.ok(authService.kakaoLogin(request.accessToken()));
    }

    @PostMapping("/reissue")
    public ResponseEntity<AuthResponse> reissue(@RequestHeader("Authorization") String refreshToken) {
        if (!refreshToken.startsWith("Bearer ")) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST);
        }
        return ResponseEntity.ok(authService.reissue(refreshToken.substring(7)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String accessToken) {
        if (!accessToken.startsWith("Bearer ")) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST);
        }
        authService.logout(accessToken.substring(7));
        return ResponseEntity.noContent().build();
    }
}