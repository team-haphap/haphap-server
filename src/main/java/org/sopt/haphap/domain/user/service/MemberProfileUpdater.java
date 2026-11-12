package org.sopt.haphap.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.user.dto.MemberProfileResponse;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.user.repository.UserRepository;
import org.sopt.haphap.global.code.GlobalErrorCode;
import org.sopt.haphap.global.code.MemberErrorCode;
import org.sopt.haphap.global.exception.CustomException;
import org.sopt.haphap.global.util.ConstraintViolations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// 프로필 수정의 DB 반영만 짧은 트랜잭션으로. S3 업로드/삭제는 호출 측(MemberProfileService)에서 트랜잭션 밖으로 뺀다.
@Component
@RequiredArgsConstructor
public class MemberProfileUpdater {

    private final UserRepository userRepository;

    public record Result(MemberProfileResponse response, String previousProfileImageUrl) {}

    @Transactional
    public Result update(Long userId, String anonymousName, String profileImageUrl) {
        // 같은 유저의 동시 수정을 줄 세운다 (이전 이미지 URL을 두 요청이 같이 읽어 고아 이미지가 생기는 것 방지)
        User user = userRepository.findByIdForUpdate(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.USER_NOT_FOUND));
        String previousProfileImageUrl = user.getProfileImageUrl();

        user.updateProfile(anonymousName, profileImageUrl);
        try {
            userRepository.flush();   // 유니크 인덱스 위반을 여기서 잡기 위해 즉시 반영
        } catch (DataIntegrityViolationException e) {
            // 사전 중복 확인과 저장 사이에 다른 유저가 같은 닉네임을 먼저 저장한 경우만 409. 다른 무결성 위반은 그대로 던진다.
            if (ConstraintViolations.isViolationOf(e, NicknamePolicy.UNIQUE_CONSTRAINT)) {
                throw new CustomException(MemberErrorCode.DUPLICATE_NICKNAME);
            }
            throw e;
        }
        return new Result(MemberProfileResponse.from(user), previousProfileImageUrl);
    }
}
