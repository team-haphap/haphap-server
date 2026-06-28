package org.sopt.haphap.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.user.dto.AuthResponse;
import org.sopt.haphap.domain.user.dto.KakaoLoginRequest;
import org.sopt.haphap.domain.user.service.AuthService;
import org.sopt.haphap.global.code.GlobalErrorCode;
import org.sopt.haphap.global.exception.CustomException;
import org.sopt.haphap.global.jwt.BearerTokenExtractor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/kakao")
    public ResponseEntity<AuthResponse> KakaoLogin(@Valid @RequestBody KakaoLoginRequest request) {
        return ResponseEntity.ok(authService.kakaoLogin(request.accessToken()));
    }

    @PostMapping("/reissue")
    public ResponseEntity<AuthResponse> reissue(@RequestHeader("Authorization") String refreshToken) {
        String token = BearerTokenExtractor.extract(refreshToken);
        if (token == null) { throw new CustomException(GlobalErrorCode.BAD_REQUEST); }
        return ResponseEntity.ok(authService.reissue(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String accessToken) {
            String token = BearerTokenExtractor.extract(authorization);
            if (token == null) { throw new CustomException(GlobalErrorCode.BAD_REQUEST); }
            authService.logout(token);
        return ResponseEntity.noContent().build();
    }
}