package org.sopt.haphap.domain.user.service;

import org.sopt.haphap.domain.user.dto.AgreementSubmitRequest;
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
    private final AgreementService agreementService;
    private final JwtProvider jwtProvider;
    private final TokenService tokenService;
    private final Map<Provider, OAuthClient> oAuthClients;

    public AuthService(UserService userService, AgreementService agreementService, JwtProvider jwtProvider,
                       TokenService tokenService, List<OAuthClient> oAuthClientList) {
        this.userService = userService;
        this.agreementService = agreementService;
        this.jwtProvider = jwtProvider;
        this.tokenService = tokenService;
        this.oAuthClients = oAuthClientList.stream()
                .collect(Collectors.toMap(OAuthClient::getProvider, c -> c));
    }

    public AuthResponse kakaoLogin(String kakaoAccessToken) {

        OAuthUserInfo userInfo = oAuthClients.get(Provider.KAKAO).getUserInfo(kakaoAccessToken);

        UserService.FindOrCreateResult result = userService.findOrCreate(
                Provider.KAKAO, userInfo.providerId(), userInfo.name(),
                userInfo.email(), userInfo.birthDate()
        );

        User user = result.user();

        // 신규 유저이거나, row는 있지만 약관 동의를 끝내지 못한 유저는
        // 정식 세션 대신 signupToken만 내려준다.. (뭔가 에러날 것 같아서 엄격(?)하게 해놨어요)

        if (result.isNew() || !user.isSignupCompleted()) {
            String signupToken = jwtProvider.createSignupToken(user.getId());
            return new AuthResponse(null, null, signupToken, user.getAnonymousName(), true);
        }

        String newRefreshToken = tokenService.issueRefreshToken(user.getId());
        return new AuthResponse(
                jwtProvider.createAccessToken(user.getId()),
                newRefreshToken,
                null,
                user.getAnonymousName(),
                false
        );
    }

    @Transactional
    public AuthResponse completeSignup(String signupToken, AgreementSubmitRequest request) {
        if (!jwtProvider.validateSignupToken(signupToken)) {
            throw new CustomException(AuthErrorCode.INVALID_SIGNUP_TOKEN);
        }
        Long userId = jwtProvider.getUserId(signupToken);
        User user = userService.findById(userId);

        agreementService.saveAgreements(user, request);
        user.completeSignup();

        String newRefreshToken = tokenService.issueRefreshToken(userId);
        return new AuthResponse(
                jwtProvider.createAccessToken(userId),
                newRefreshToken,
                null,
                user.getAnonymousName(),
                false
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
                null,
                user.getAnonymousName(),
                false
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