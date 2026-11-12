package org.sopt.haphap.domain.user.dto;

import org.sopt.haphap.domain.user.entity.Provider;
import org.sopt.haphap.domain.user.entity.User;

// 계정 정보: 소셜 로그인으로 받은 실제 이름·이메일과 로그인 수단 (닉네임 아님)
public record MemberAccountResponse(
        String name,
        String email,
        Provider provider
) {
    public static MemberAccountResponse from(User user) {
        return new MemberAccountResponse(user.getName(), user.getEmail(), user.getProvider());
    }
}
