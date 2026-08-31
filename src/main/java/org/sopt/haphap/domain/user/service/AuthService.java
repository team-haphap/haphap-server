package org.sopt.haphap.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.user.dto.AuthResponse;
import org.sopt.haphap.domain.user.entity.Provider;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.global.client.AppleOAuthClient;
import org.sopt.haphap.global.client.OAuthClient;
import org.sopt.haphap.global.client.dto.OAuthUserInfo;
import org.sopt.haphap.global.exception.CustomException;
import org.sopt.haphap.global.jwt.JwtProvider;
import org.sopt.haphap.global.jwt.TokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sopt.haphap.global.code.AuthErrorCode;
import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.Map;
import org.sopt.haphap.global.jwt.Role;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j

public class AuthService {

    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final TokenService tokenService;
    private final List<OAuthClient> oAuthClientList;
    private final AppleOAuthClient appleOAuthClient;

    private Map<Provider, OAuthClient> oAuthClients;
    @PostConstruct
    private void initOAuthClients() {
        this.oAuthClients = oAuthClientList.stream()
                .collect(Collectors.toMap(OAuthClient::getProvider, c -> c));
    }

    public AuthResponse kakaoLogin(String kakaoAccessToken) {
        OAuthUserInfo userInfo = oAuthClients.get(Provider.KAKAO).getUserInfo(kakaoAccessToken);
        UserService.FindOrCreateResult result = userService.findOrCreate(
                Provider.KAKAO, userInfo.providerId(), userInfo
        );
        User user = result.user();
        String newRefreshToken = tokenService.issueRefreshToken(user.getId(), Role.USER);
        return new AuthResponse(
                jwtProvider.createAccessToken(user.getId()),
                newRefreshToken,
                user.getName(),
                user.getAnonymousName(),
                user.getProfileImageUrl()
        );
    }

    public AuthResponse appleLogin(String identityToken, String authorizationCode, String name) {
        OAuthUserInfo userInfo = oAuthClients.get(Provider.APPLE).getUserInfo(identityToken);

        if (userInfo.name() == null && name != null) {
            userInfo = new OAuthUserInfo(
                    userInfo.providerId(), name, userInfo.email(),
                    userInfo.birthDate(), userInfo.gender(), userInfo.ageRange(), userInfo.phoneNumber()
            );
        }

        UserService.FindOrCreateResult result = userService.findOrCreate(Provider.APPLE, userInfo.providerId(), userInfo);
        User user = result.user();

        try {
            String appleRefreshToken = appleOAuthClient.exchangeAuthorizationCode(authorizationCode);
            userService.updateAppleRefreshToken(user.getId(), appleRefreshToken);
        } catch (Exception e) {
            log.warn("애플 refresh token 교환 실패 - 로그인은 계속 진행합니다. userId={}", user.getId(), e);
        }

        String newRefreshToken = tokenService.issueRefreshToken(user.getId(), Role.USER);
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
        if (!tokenService.isValid(userId, Role.USER, refreshToken)) {
            throw new CustomException(AuthErrorCode.REFRESH_TOKEN_MISMATCH);
        }
        User user = userService.findById(userId);
        String newRefreshToken = tokenService.issueRefreshToken(userId, Role.USER);
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
        tokenService.deleteRefreshToken(userId, Role.USER);
    }
}