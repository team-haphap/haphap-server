package org.sopt.haphap.domain.user.service;

import org.sopt.haphap.domain.user.dto.AuthResponse;
import org.sopt.haphap.domain.user.entity.Provider;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.global.client.OAuthClient;
import org.sopt.haphap.global.client.dto.OAuthUserInfo;
import org.sopt.haphap.global.exception.CustomException;
import org.sopt.haphap.global.jwt.JwtProvider;
import org.sopt.haphap.global.jwt.TokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sopt.haphap.global.code.AuthErrorCode;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final TokenService tokenService;
    private final Map<Provider, OAuthClient> oAuthClients;

    public AuthService(UserService userService, JwtProvider jwtProvider,
                       TokenService tokenService, List<OAuthClient> oAuthClientList) {
        this.userService = userService;
        this.jwtProvider = jwtProvider;
        this.tokenService = tokenService;
        this.oAuthClients = oAuthClientList.stream()
                .collect(Collectors.toMap(OAuthClient::getProvider, c -> c));
    }

    public AuthResponse kakaoLogin(String kakaoAccessToken) {
        OAuthUserInfo userInfo = oAuthClients.get(Provider.KAKAO).getUserInfo(kakaoAccessToken);
        UserService.FindOrCreateResult result = userService.findOrCreate(
                Provider.KAKAO, userInfo.providerId(), userInfo
        );
        User user = result.user();
        String newRefreshToken = tokenService.issueRefreshToken(user.getId());
        return new AuthResponse(
                jwtProvider.createAccessToken(user.getId()),
                newRefreshToken,
                user.getName(),
                user.getAnonymousName(),
                user.getProfileImageUrl()
        );
    }

    @Transactional
    public AuthResponse reissue(String refreshToken) {
        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
        Long userId = jwtProvider.getUserId(refreshToken);
        if (!tokenService.isValid(userId, refreshToken)) {
            throw new CustomException(AuthErrorCode.REFRESH_TOKEN_MISMATCH);
        }
        User user = userService.findById(userId);
        String newRefreshToken = tokenService.issueRefreshToken(userId);
        return new AuthResponse(
                jwtProvider.createAccessToken(userId),
                newRefreshToken,
                user.getName(),
                user.getAnonymousName(),
                user.getProfileImageUrl()
        );
    }

    public void logout(String accessToken) {
        boolean expired = jwtProvider.isExpiredAccessToken(accessToken);
        if (!expired && !jwtProvider.validateAccessToken(accessToken)) {
            throw new CustomException(AuthErrorCode.INVALID_ACCESS_TOKEN);
        }
        Long userId = jwtProvider.getUserIdIgnoringExpiration(accessToken);
        if (!expired) {
            tokenService.blacklistAccessToken(accessToken);
        }
        tokenService.deleteRefreshToken(userId);
    }
}