package org.sopt.haphap.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.user.dto.AuthResponse;
import org.sopt.haphap.domain.user.entity.Provider;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.user.repository.UserRepository;
import org.sopt.haphap.global.client.KakaoApiClient;
import org.sopt.haphap.global.code.GlobalErrorCode;
import org.sopt.haphap.global.exception.CustomException;
import org.sopt.haphap.global.jwt.JwtProvider;
import org.sopt.haphap.global.util.AnonymousNameGenerator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final WebClient webClient;
    private final RedisTemplate<String, String> redisTemplate;
    private final AnonymousNameGenerator anonymousNameGenerator;

    @Transactional
    public AuthResponse kakaoLogin(String kakaoAccessToken) {
        // 1. 카카오에서 유저 정보 조회
        Map<String, Object> kakaoUser = getKakaoUserInfo(kakaoAccessToken);
        String providerId = String.valueOf(kakaoUser.get("id"));

        Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoUser.get("kakao_account");
        String name = (String) kakaoAccount.get("name");
        String email = (String) kakaoAccount.get("email");
        String birthyear = (String) kakaoAccount.getOrDefault("birthyear", "");
        String birthday = (String) kakaoAccount.getOrDefault("birthday", "");
        LocalDate birthDate = parseBirthDate(birthyear, birthday);

        // 2. DB에서 유저 찾기
        boolean[] isNew = {false};
        User user = userRepository.findByProviderAndProviderId(Provider.KAKAO, providerId)
                .orElseGet(() -> {
                    isNew[0] = true;
                    return userRepository.save(
                            User.builder()
                                    .provider(Provider.KAKAO)
                                    .providerId(providerId)
                                    .name(name)
                                    .email(email)
                                    .birthDate(birthDate)
                                    .anonymousName(anonymousNameGenerator.generate())
                                    .build()
                    );
                });

        // 3. JWT 발급
        return new AuthResponse(
                jwtProvider.createAccessToken(user.getId()),
                jwtProvider.createRefreshToken(user.getId()),
                user.getAnonymousName(),
                isNew[0]
        );
    }

    @Transactional
    public AuthResponse reissue(String refreshToken) {
        if (!jwtProvider.validate(refreshToken)) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST);
        }
        Long userId = jwtProvider.getUserId(refreshToken);
        String stored = redisTemplate.opsForValue().get("refresh:" + userId);
        if (!refreshToken.equals(stored)) {
            throw new IllegalArgumentException("일치하지 않는 refreshToken");
        }
        User user = userRepository.findById(userId).orElseThrow();
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

    private final KakaoApiClient kakaoApiClient; {
        return webClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
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