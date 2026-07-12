package org.sopt.haphap.global.util;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.user.entity.ProfileImage;
import org.sopt.haphap.domain.user.repository.ProfileImageRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class ProfileImageAssigner {

    private final ProfileImageRepository profileImageRepository;

    public String assign() {
        List<ProfileImage> images = profileImageRepository.findAll();
        if (images.isEmpty()) {
            throw new IllegalStateException("등록된 프로필 이미지가 없습니다.");
        }
        int index = ThreadLocalRandom.current().nextInt(images.size());
        return images.get(index).getImageUrl();
    }
}