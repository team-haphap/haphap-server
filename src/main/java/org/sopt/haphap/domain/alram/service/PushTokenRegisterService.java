package org.sopt.haphap.domain.alram.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.alram.domain.PushToken;
import org.sopt.haphap.domain.alram.dto.PushTokenRegisterRequest;
import org.sopt.haphap.domain.alram.repository.PushTokenRepository;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.user.repository.UserRepository;
import org.sopt.haphap.global.code.GlobalErrorCode;
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
                .orElseThrow(() -> new CustomException(GlobalErrorCode.USER_NOT_FOUND));

        // (유저, 이 기기)의 토큰이 이미 있으면 갱신, 없으면 새로 생성
        pushTokenRepository.findByUserIdAndDeviceId(userId, request.deviceId())
                .ifPresentOrElse(
                        token -> token.renew(request.fcmToken(), request.deviceType()),
                        () -> pushTokenRepository.save(PushToken.create(
                                user, request.deviceId(), request.fcmToken(), request.deviceType()))
                );
    }
}
