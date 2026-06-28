package org.sopt.haphap.global.jwt;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RefreshTokenStore {

    private static final String KEY_PREFIX = "refresh:";
    private static final long REFRESH_TOKEN_EXPIRY = 1000L * 60 * 60 * 24 * 14;

    private final RedisTemplate<String, String> redisTemplate;

    public void save(Long userId, String token) {
        redisTemplate.opsForValue().set(
                KEY_PREFIX + userId, token, REFRESH_TOKEN_EXPIRY, TimeUnit.MILLISECONDS
        );
    }

    public String get(Long userId) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + userId);
    }

    public void delete(Long userId) {
        redisTemplate.delete(KEY_PREFIX + userId);
    }

    public boolean isValid(Long userId, String token) {
        return Objects.equals(get(userId), token);
    }
}