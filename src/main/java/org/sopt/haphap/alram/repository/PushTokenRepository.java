package org.sopt.haphap.alram.repository;

import java.util.Collection;
import java.util.List;
import org.sopt.haphap.alram.domain.PushToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushTokenRepository extends JpaRepository<PushToken, Long> {
    List<PushToken> findByUserIdAndActiveTrue(Long userId);

    List<PushToken> findAllByUserIdInAndActiveTrue(Collection<Long> userIds);
}