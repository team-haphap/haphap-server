package org.sopt.haphap.global.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";

    public String issueRefreshToken(Long userId) {
        String refreshToken = jwtProvider.createRefreshToken(userId);
        refreshTokenStore.save(userId, refreshToken);
        return refreshToken;
    }
    public boolean isValid(Long userId, String refreshToken) {
        return refreshTokenStore.isValid(userId, refreshToken);
    }

    public void deleteRefreshToken(Long userId) {
        refreshTokenStore.delete(userId);
    }

    public void blacklistAccessToken(String accessToken) {
        long expiry = jwtProvider.getExpiration(accessToken); // 남은 만료 시간 (ms)
        if (expiry > 0) {
            redisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + accessToken,
                    "logout",
                    expiry,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    public boolean isBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + accessToken));
    }
}