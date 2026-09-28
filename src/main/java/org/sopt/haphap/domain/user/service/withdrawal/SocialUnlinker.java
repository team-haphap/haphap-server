package org.sopt.haphap.domain.user.service.withdrawal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.global.client.AppleOAuthClient;
import org.sopt.haphap.global.client.KakaoOAuthClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SocialUnlinker {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final AppleOAuthClient appleOAuthClient;

    public void unlink(User user) {
        switch (user.getProvider()) {
            case KAKAO -> kakaoOAuthClient.unlink(user.getProviderId());
            case APPLE -> revokeApple(user);
            case LOCAL -> { } // 외부 연동 없음
        }
    }

    private void revokeApple(User user) {
        if (user.getAppleRefreshToken() == null) {
            // refresh token 저장 이전에 가입한 유저는 revoke 불가. 탈퇴 자체는 막지 않음
            log.warn("애플 refresh token 없음, revoke 생략 userId={}", user.getId());
            return;
        }
        appleOAuthClient.revoke(user.getAppleRefreshToken());
    }
}