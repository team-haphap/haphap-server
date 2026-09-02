package org.sopt.haphap.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.user.dto.MemberResponse;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.global.jwt.Role;
import org.sopt.haphap.global.jwt.TokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final UserService userService;
    private final TokenService tokenService;

    public MemberResponse getMyInfo(Long userId) {
        User user = userService.findById(userId);
        return MemberResponse.from(user);
    }

    @Transactional
    public void requestWithdraw(Long userId, String accessToken) {
        User user = userService.findById(userId);

        tokenService.blacklistAccessToken(accessToken);
        tokenService.deleteRefreshToken(userId, Role.USER);
        user.markPendingUnlink();
    }
}