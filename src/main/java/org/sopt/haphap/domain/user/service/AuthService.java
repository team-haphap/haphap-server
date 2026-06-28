package org.sopt.haphap.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.user.dto.AuthResponse;
import org.sopt.haphap.domain.user.entity.Provider;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.global.client.KakaoApiClient;
import org.sopt.haphap.global.client.dto.KakaoUserResponse;
import org.sopt.haphap.global.code.GlobalErrorCode;
import org.sopt.haphap.global.exception.CustomException;
import org.sopt.haphap.global.jwt.JwtProvider;
import org.springframework.stereotype.Service;
import org.sopt.haphap.global.jwt.RefreshTokenStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final KakaoApiClient kakaoApiClient;

    // @Transactional 없음 — 외부 HTTP 호출이 트랜잭션 밖에서 실행됨
    public AuthResponse kakaoLogin(String kakaoAccessToken) {

        // 1. 카카오에서 유저 정보 조회 (HTTP, 트랜잭션 밖)
        KakaoUserResponse kakaoUser = kakaoApiClient.getUserInfo(kakaoAccessToken);

        KakaoUserResponse.KakaoAccount account = kakaoUser.kakaoAccount();
        if (account == null) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST);
        }

        String providerId = String.valueOf(kakaoUser.id());
        LocalDate birthDate = parseBirthDate(
                account.birthyear() != null ? account.birthyear() : "",
                account.birthday() != null ? account.birthday() : ""
        );

        // 2. DB 유저 조회/생성
        UserService.FindOrCreateResult result =
                userService.findOrCreate(Provider.KAKAO, providerId, account.name(), account.email(), birthDate);

        // 3. JWT 발급
        Long userId = result.user().getId();
        String newRefreshToken = jwtProvider.createRefreshToken(userId);
        refreshTokenStore.save(userId, newRefreshToken);

        return new AuthResponse(
                jwtProvider.createAccessToken(userId),
                newRefreshToken,
                result.user().getAnonymousName(),
                result.isNew()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse reissue(String refreshToken) {
        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST);
        }
        Long userId = jwtProvider.getUserId(refreshToken);
        if (!refreshTokenStore.isValid(userId, refreshToken)) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST);
        }
        User user = userService.findById(userId);
        String newRefreshToken = jwtProvider.createRefreshToken(userId);
        refreshTokenStore.save(userId, newRefreshToken);
        return new AuthResponse(
                jwtProvider.createAccessToken(userId),
                newRefreshToken,
                user.getAnonymousName(),
                false
        );
    }

    public void logout(String accessToken) {
        if (!jwtProvider.validateAccessToken(accessToken)) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST);
        }
        Long userId = jwtProvider.getUserId(accessToken);
        refreshTokenStore.delete(userId);
    }

    private LocalDate parseBirthDate(String birthyear, String birthday) {
        try {
            return LocalDate.parse(birthyear + birthday, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            log.warn("생년월일 파싱 실패: birthyear={}, birthday={}", birthyear, birthday);
            return null;
        }
    }
}
