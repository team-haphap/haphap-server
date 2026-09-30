package org.sopt.haphap.domain.user.dto;

import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.user.entity.Provider;

public record MemberResponse(
        String name,
        String anonymousName,
        String email,
        String profileImageUrl,
        Provider provider
) {
    public static MemberResponse from(User user) {
        return new MemberResponse(
                user.getName(),
                user.getAnonymousName(),
                user.getEmail(),
                user.getProfileImageUrl(),
                user.getProvider()
        );
    }
}