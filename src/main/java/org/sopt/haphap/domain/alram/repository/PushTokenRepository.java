package org.sopt.haphap.domain.alram.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.sopt.haphap.domain.alram.domain.PushToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushTokenRepository extends JpaRepository<PushToken, Long> {
    List<PushToken> findByUserIdAndActiveTrue(Long userId);
    Optional<PushToken> findByFcmToken(String fcmToken);
    List<PushToken> findAllByUserIdInAndActiveTrue(Collection<Long> userIds);
}