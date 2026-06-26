package org.sopt.haphap.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.user.dto.AuthResponse;
import org.sopt.haphap.domain.user.entity.Provider;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.user.repository.UserRepository;
import org.sopt.haphap.global.jwt.JwtProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final WebClient webClient;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public AuthResponse kakaoLogin(String kakaoAccessToken) {
        // 1. 카카오에서 유저 정보 조회
        Map<String, Object> kakaoUser = getKakaoUserInfo(kakaoAccessToken);
        String providerId = String.valueOf(kakaoUser.get("id"));
        Map<String, Object> properties = (Map<String, Object>) kakaoUser.get("properties");
        String nickname = (String) properties.get("nickname");

        // 2. DB에서 유저 찾기 (없으면 회원가입)
        User user = userRepository.findByProviderAndProviderId(Provider.KAKAO, providerId)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .provider(Provider.KAKAO)
                                .providerId(providerId)
                                .nickname(nickname)
                                .build()
                ));

        // 3. JWT 발급
        return new AuthResponse(
                jwtProvider.createAccessToken(user.getId()),
                jwtProvider.createRefreshToken(user.getId())
        );
    }

    @Transactional
    public AuthResponse reissue(String refreshToken) {
        if (!jwtProvider.validate(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 refreshToken");
        }
        Long userId = jwtProvider.getUserId(refreshToken);

        String stored = redisTemplate.opsForValue().get("refresh:" + userId);
        if (!refreshToken.equals(stored)) {
            throw new IllegalArgumentException("일치하지 않는 refreshToken");
        }

        return new AuthResponse(
                jwtProvider.createAccessToken(userId),
                jwtProvider.createRefreshToken(userId)
        );
    }

    public void logout(String accessToken) {
        Long userId = jwtProvider.getUserId(accessToken);
        redisTemplate.delete("refresh:" + userId);
    }

    private Map<String, Object> getKakaoUserInfo(String accessToken) {
        return webClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}