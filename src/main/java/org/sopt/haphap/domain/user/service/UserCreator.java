package org.sopt.haphap.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.user.entity.Provider;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.user.repository.UserRepository;
import org.sopt.haphap.global.client.dto.OAuthUserInfo;
import org.sopt.haphap.global.code.AuthErrorCode;
import org.sopt.haphap.global.exception.CustomException;
import org.sopt.haphap.global.util.NicknameAssigner;
import org.sopt.haphap.global.util.ProfileImageAssigner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// 신규 가입 INSERT만 독립 트랜잭션으로 실행
// 동시 첫 로그인으로 유니크 제약이 깨져도 이 트랜잭션만 롤백되고, 호출 측은 재조회로 복구할 수 있도록

@Component
@RequiredArgsConstructor
public class UserCreator {

    private final UserRepository userRepository;
    private final NicknameAssigner nicknameAssigner;
    private final ProfileImageAssigner profileImageAssigner;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public User create(Provider provider, String providerId, OAuthUserInfo userInfo) {
        if (userInfo.email() == null) {
            throw new CustomException(AuthErrorCode.EMAIL_REQUIRED);
        }
        if (userInfo.name() == null) {
            throw new CustomException(AuthErrorCode.NAME_REQUIRED);
        }
        return userRepository.saveAndFlush(   // 제약 위반을 이 메서드 안에서 처리
                User.builder()
                        .provider(provider)
                        .providerId(providerId)
                        .name(userInfo.name())
                        .email(userInfo.email())
                        .birthDate(userInfo.birthDate())
                        .gender(userInfo.gender())
                        .ageRange(userInfo.ageRange())
                        .phoneNumber(userInfo.phoneNumber())
                        .anonymousName(nicknameAssigner.assign())
                        .profileImageUrl(profileImageAssigner.assign())
                        .build()
        );
    }
}