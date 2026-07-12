package org.sopt.haphap.domain.user.repository;

import org.sopt.haphap.domain.user.entity.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {
}