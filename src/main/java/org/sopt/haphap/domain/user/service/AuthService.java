package org.sopt.haphap.domain.user.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.user.dto.AuthResponse;
import org.sopt.haphap.domain.user.entity.Provider;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.global.client.AppleOAuthClient;
import org.sopt.haphap.global.client.OAuthClient;
import org.sopt.haphap.global.client.dto.AppleTokenExchangeResult;
import org.sopt.haphap.global.client.dto.OAuthUserInfo;
import org.sopt.haphap.global.code.AuthErrorCode;
import org.sopt.haphap.global.exception.CustomException;
import org.sopt.haphap.global.jwt.JwtProvider;
import org.sopt.haphap.global.jwt.Role;
import org.sopt.haphap.global.jwt.TokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
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
    public AuthResponse appleLogin(String authorizationCode, String identityToken, String name) {
        // 1. identity token 검증 → 어느 애플 사용자인지 확정
        OAuthUserInfo userInfo = oAuthClients.get(Provider.APPLE).getUserInfo(identityToken);
        if (userInfo.name() == null && name != null) {
            userInfo = new OAuthUserInfo(
                    userInfo.providerId(), name, userInfo.email(),
                    userInfo.birthDate(), userInfo.gender(), userInfo.ageRange(), userInfo.phoneNumber()
            );
        }

        // 2. 인가 코드 교환 → refresh token(연동 해제용) + id token(검증용)
        AppleTokenExchangeResult exchanged = appleOAuthClient.exchangeAuthorizationCode(authorizationCode);

        // 3. 두 토큰이 같은 사용자 것인지 확인 (다른 사람의 인가 코드를 섞어 보내는 것 방지)
        String exchangedSubject = oAuthClients.get(Provider.APPLE).getUserInfo(exchanged.idToken()).providerId();
        if (!userInfo.providerId().equals(exchangedSubject)) {
            log.warn("애플 토큰 사용자 불일치 identitySub={}, exchangedSub={}", userInfo.providerId(), exchangedSubject);
            throw new CustomException(AuthErrorCode.APPLE_INVALID_TOKEN);
        }

        // 4. 가입 또는 로그인 + refresh token 저장
        UserService.FindOrCreateResult result = userService.findOrCreate(Provider.APPLE, userInfo.providerId(), userInfo);
        User user = result.user();
        userService.updateAppleRefreshToken(user.getId(), exchanged.refreshToken());

        String newRefreshToken = tokenService.issueRefreshToken(user.getId(), Role.USER);
        return new AuthResponse(jwtProvider.createAccessToken(user.getId()), newRefreshToken,
                user.getName(), user.getAnonymousName(), user.getProfileImageUrl());
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
        if (!user.isActive()) {
            tokenService.deleteRefreshToken(userId, Role.USER);   // 탈퇴(처리 중 포함) 회원의 남은 토큰 정리
            throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
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