package org.sopt.haphap.global.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenStore refreshTokenStore;

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
}