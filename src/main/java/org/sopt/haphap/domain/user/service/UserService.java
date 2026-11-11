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

    private static final int MAX_SIGNUP_ATTEMPTS = 3;

    // User + isNew를 함께 담는 record
    public record FindOrCreateResult(User user, boolean isNew) {}

    public FindOrCreateResult findOrCreate(Provider provider, String providerId, OAuthUserInfo userInfo) {
        Optional<User> existing = userRepository.findByProviderAndProviderId(provider, providerId);
        if (existing.isPresent()) {
            return new FindOrCreateResult(requireActive(existing.get()), false);
        }
        for (int attempt = 1; attempt <= MAX_SIGNUP_ATTEMPTS; attempt++) {
            try {
                return new FindOrCreateResult(userCreator.create(provider, providerId, userInfo), true);
            } catch (DataIntegrityViolationException e) {
                // 동시 첫 로그인: 다른 요청이 먼저 가입시킴 → 그 유저로 로그인 처리
                Optional<User> created = userRepository.findByProviderAndProviderId(provider, providerId);
                if (created.isPresent()) {
                    return new FindOrCreateResult(requireActive(created.get()), false);
                }
                // 그 유저가 없으면 닉네임 유니크 충돌(동시 가입자가 같은 닉네임을 뽑음) → 닉네임을 새로 뽑아 재시도
            }
        }
        throw new CustomException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
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