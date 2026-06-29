package org.sopt.haphap.domain.alram.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.alram.domain.PushToken;
import org.sopt.haphap.domain.alram.dto.PushTokenRegisterRequest;
import org.sopt.haphap.domain.alram.repository.PushTokenRepository;
import org.sopt.haphap.domain.member.domain.User;
import org.sopt.haphap.domain.member.repository.UserRepository;
import org.sopt.haphap.domain.registration.code.RegistrationErrorCode;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PushTokenRegisterService {

    private final PushTokenRepository pushTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public void register(Long userId, PushTokenRegisterRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(RegistrationErrorCode.USER_NOT_FOUND));

        // 같은 토큰이 이미 있으면 갱신(재활성화), 없으면 새로 생성
        pushTokenRepository.findByFcmToken(request.fcmToken())
                .ifPresentOrElse(
                        token -> token.activate(request.deviceType()),
                        () -> pushTokenRepository.save(
                                PushToken.create(user, request.fcmToken(), request.deviceType()))
                );
    }
}
