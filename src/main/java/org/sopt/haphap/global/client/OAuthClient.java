package org.sopt.haphap.global.client;

import org.sopt.haphap.domain.user.entity.Provider;
import org.sopt.haphap.global.client.dto.OAuthUserInfo;

public interface OAuthClient {
    Provider getProvider();
    OAuthUserInfo getUserInfo(String accessToken);
}