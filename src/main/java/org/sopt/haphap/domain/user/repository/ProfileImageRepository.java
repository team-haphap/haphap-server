package org.sopt.haphap.domain.user.repository;

import org.sopt.haphap.domain.user.entity.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {

    // 가입 시 배정되는 기본(프리셋) 이미지인지. 여러 유저가 공유하므로 교체돼도 지우면 안 된다.
    boolean existsByImageUrl(String imageUrl);
}