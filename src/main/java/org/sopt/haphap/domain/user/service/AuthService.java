package org.sopt.haphap.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.user.dto.AuthResponse;
import org.sopt.haphap.domain.user.entity.Provider;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.global.client.KakaoApiClient;
import org.sopt.haphap.global.code.GlobalErrorCode;
import org.sopt.haphap.global.exception.CustomException;
import org.sopt.haphap.global.jwt.JwtProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final KakaoApiClient kakaoApiClient;

    // @Transactional 없음 — 외부 HTTP 호출이 트랜잭션 밖에서 실행됨
    public AuthResponse kakaoLogin(String kakaoAccessToken) {
        // 1. 카카오에서 유저 정보 조회 (HTTP, 트랜잭션 밖)
        Map<String, Object> kakaoUser = kakaoApiClient.getUserInfo(kakaoAccessToken);
        String providerId = String.valueOf(kakaoUser.get("id"));

        Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoUser.get("kakao_account");
        if (kakaoAccount == null) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST);
        }

        String name = (String) kakaoAccount.get("name");
        String email = (String) kakaoAccount.get("email");
        String birthyear = (String) kakaoAccount.getOrDefault("birthyear", "");
        String birthday = (String) kakaoAccount.getOrDefault("birthday", "");
        LocalDate birthDate = parseBirthDate(birthyear, birthday);

        // 2. DB 유저 조회/생성
        UserService.FindOrCreateResult result =
                userService.findOrCreate(Provider.KAKAO, providerId, name, email, birthDate);

        // 3. JWT 발급
        return new AuthResponse(
                jwtProvider.createAccessToken(result.user().getId()),
                jwtProvider.createRefreshToken(result.user().getId()),
                result.user().getAnonymousName(),
                result.isNew()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse reissue(String refreshToken) {
        if (!jwtProvider.validate(refreshToken)) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST);
        }
        Long userId = jwtProvider.getUserId(refreshToken);
        String stored = redisTemplate.opsForValue().get("refresh:" + userId);
        if (!refreshToken.equals(stored)) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST);
        }
        User user = userService.findById(userId);
        return new AuthResponse(
                jwtProvider.createAccessToken(userId),
                jwtProvider.createRefreshToken(userId),
                user.getAnonymousName(),
                false
        );
    }

    public void logout(String accessToken) {
        Long userId = jwtProvider.getUserId(accessToken);
        redisTemplate.delete("refresh:" + userId);
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