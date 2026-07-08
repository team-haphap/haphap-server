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
    //TODO. 토큰을 바꾸어요
    private static final long REFRESH_TOKEN_EXPIRY = 1000L * 60 * 60 * 24 * 90;

    private final RedisTemplate<String, String> redisTemplate;

    public void save(Long id, Role role, String token) {
        redisTemplate.opsForValue().set(
                key(id, role), token, REFRESH_TOKEN_EXPIRY, TimeUnit.MILLISECONDS
        );
    }

    public String get(Long id, Role role) {
        return redisTemplate.opsForValue().get(key(id, role));
    }

    public void delete(Long id, Role role) {
        redisTemplate.delete(key(id, role));
    }

    public boolean isValid(Long id, Role role, String token) {
        return Objects.equals(get(id, role), token);
    }

    private String key(Long id, Role role) {
        return KEY_PREFIX + role.name() + ":" + id;
    }
}