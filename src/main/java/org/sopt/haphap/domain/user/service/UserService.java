package org.sopt.haphap.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.user.entity.Provider;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.user.repository.UserRepository;
import org.sopt.haphap.global.code.GlobalErrorCode;
import org.sopt.haphap.global.exception.CustomException;
import org.sopt.haphap.global.util.AnonymousNameGenerator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AnonymousNameGenerator anonymousNameGenerator;

    // User + isNew를 함께 담는 record
    public record FindOrCreateResult(User user, boolean isNew) {}

    @Transactional
    public FindOrCreateResult findOrCreate(Provider provider, String providerId, String name, String email, LocalDate birthDate) {
        return userRepository.findByProviderAndProviderId(provider, providerId)
                .map(user -> new FindOrCreateResult(user, false))
                .orElseGet(() -> {
                    try {
                        User newUser = userRepository.save(
                                User.builder()
                                        .provider(provider)
                                        .providerId(providerId)
                                        .name(name)
                                        .email(email)
                                        .birthDate(birthDate)
                                        .anonymousName(anonymousNameGenerator.generate())
                                        .build()
                        );
                        return new FindOrCreateResult(newUser, true);
                    } catch (DataIntegrityViolationException e) {
                        User existing = userRepository.findByProviderAndProviderId(provider, providerId)
                                .orElseThrow(() -> new CustomException(GlobalErrorCode.INTERNAL_SERVER_ERROR));
                        return new FindOrCreateResult(existing, false);
                    }
                });
    }

    @Transactional(readOnly = true)
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.USER_NOT_FOUND));
    }
}