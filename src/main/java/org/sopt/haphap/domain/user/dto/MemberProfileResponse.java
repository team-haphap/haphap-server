package org.sopt.haphap.domain.user.dto;

import org.sopt.haphap.domain.user.entity.Provider;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.user.service.NicknamePolicy;

public record MemberProfileResponse(
        String name,
        String nickname,
        String anonymousName,
        String email,
        String profileImageUrl,
        Provider provider
) {
    public static MemberProfileResponse from(User user) {
        return new MemberProfileResponse(
                user.getName(),
                NicknamePolicy.toNickname(user.getAnonymousName()),
                user.getAnonymousName(),
                user.getEmail(),
                user.getProfileImageUrl(),
                user.getProvider()
        );
    }
}
