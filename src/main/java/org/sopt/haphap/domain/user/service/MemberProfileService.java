package org.sopt.haphap.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.user.dto.MemberProfileResponse;
import org.sopt.haphap.domain.user.repository.ProfileImageRepository;
import org.sopt.haphap.domain.user.repository.UserRepository;
import org.sopt.haphap.global.code.MemberErrorCode;
import org.sopt.haphap.global.exception.CustomException;
import org.sopt.haphap.global.s3.ImageCategory;
import org.sopt.haphap.global.s3.S3Uploader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

// 트랜잭션을 걸지 않는다: 이미지 변환·업로드 동안 DB 커넥션/행 잠금을 잡지 않도록
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberProfileService {

    private final UserRepository userRepository;
    private final ProfileImageRepository profileImageRepository;
    private final MemberProfileUpdater memberProfileUpdater;
    private final S3Uploader s3Uploader;

    // nickname, profileImage 모두 선택. 보낸 항목만 바꾼다.
    public MemberProfileResponse updateProfile(Long userId, String nickname, MultipartFile profileImage) {
        String anonymousName = nickname == null ? null : NicknamePolicy.toAnonymousName(nickname);

        // 중복이면 이미지를 올리기 전에 끊는다. 동시 요청은 유니크 인덱스가 최종 방어 (MemberProfileUpdater)
        if (anonymousName != null && userRepository.existsActiveAnonymousNameExcept(anonymousName, userId)) {
            throw new CustomException(MemberErrorCode.DUPLICATE_NICKNAME);
        }

        String uploadedUrl = hasFile(profileImage)
                ? s3Uploader.upload(profileImage, ImageCategory.PROFILE.getDirName())
                : null;

        MemberProfileUpdater.Result result;
        try {
            result = memberProfileUpdater.update(userId, anonymousName, uploadedUrl);
        } catch (RuntimeException e) {
            deleteQuietly(uploadedUrl);   // DB 반영 실패 → 방금 올린 이미지는 고아가 되므로 보상 삭제
            throw e;
        }

        if (uploadedUrl != null) {
            deleteReplacedImage(result.previousProfileImageUrl());   // 커밋 후에만 이전 이미지 삭제
        }
        return result.response();
    }

    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    // 직접 올린 이미지만 지운다. 기본(프리셋) 이미지는 여러 유저가 공유하므로 유지.
    private void deleteReplacedImage(String previousUrl) {
        if (previousUrl == null || profileImageRepository.existsByImageUrl(previousUrl)) {
            return;
        }
        deleteQuietly(previousUrl);
    }

    private void deleteQuietly(String url) {
        if (url == null) {
            return;
        }
        try {
            s3Uploader.deleteByUrl(url);
        } catch (Exception e) {
            log.warn("프로필 이미지 삭제 실패(고아 파일로 남음) url={}", url, e);
        }
    }
}
