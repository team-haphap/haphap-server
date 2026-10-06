package org.sopt.haphap.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.user.entity.Provider;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.user.repository.UserRepository;
import org.sopt.haphap.global.client.dto.OAuthUserInfo;
import org.sopt.haphap.global.code.AuthErrorCode;
import org.sopt.haphap.global.code.GlobalErrorCode;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserCreator userCreator;

    // User + isNew를 함께 담는 record
    public record FindOrCreateResult(User user, boolean isNew) {}

    public FindOrCreateResult findOrCreate(Provider provider, String providerId, OAuthUserInfo userInfo) {
        Optional<User> existing = userRepository.findByProviderAndProviderId(provider, providerId);
        if (existing.isPresent()) {
            return new FindOrCreateResult(requireActive(existing.get()), false);
        }
        try {
            return new FindOrCreateResult(userCreator.create(provider, providerId, userInfo), true);
        } catch (DataIntegrityViolationException e) {
            // 동시 첫 로그인: 다른 요청이 먼저 가입시킴 → 그 유저로 로그인 처리
            User user = userRepository.findByProviderAndProviderId(provider, providerId)
                    .orElseThrow(() -> new CustomException(GlobalErrorCode.INTERNAL_SERVER_ERROR));
            return new FindOrCreateResult(requireActive(user), false);
        }
    }

    private User requireActive(User user) {
        if (!user.isActive()) {
            throw new CustomException(AuthErrorCode.WITHDRAWAL_IN_PROGRESS);
        }
        return user;
    }

    @Transactional(readOnly = true)
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public void updateAppleRefreshToken(Long userId, String refreshToken) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.USER_NOT_FOUND));
        if (!user.isActive()) {
            throw new CustomException(AuthErrorCode.WITHDRAWAL_IN_PROGRESS);
        }
        user.updateAppleRefreshToken(refreshToken);
    }
}