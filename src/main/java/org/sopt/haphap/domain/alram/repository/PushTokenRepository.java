package org.sopt.haphap.domain.alram.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.sopt.haphap.domain.alram.domain.PushToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PushTokenRepository extends JpaRepository<PushToken, Long> {
    List<PushToken> findByUserIdAndActiveTrue(Long userId);
    Optional<PushToken> findByUserIdAndDeviceId(Long userId, String deviceId);
    List<PushToken> findAllByUserIdInAndActiveTrue(List<Long> userIds);

    @Modifying(flushAutomatically = true)
    @Query("update PushToken p set p.active = false where p.deviceId = :deviceId and p.user.id <> :userId and p.active = true")
    int deactivateOthersOnDevice(@Param("deviceId") String deviceId, @Param("userId") Long userId);

    @Modifying(flushAutomatically = true)
    @Query("delete from PushToken p where p.user.id = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);

    @Modifying(flushAutomatically = true)
    @Query("update PushToken p set p.active = false where p.user.id = :userId and p.deviceId = :deviceId and p.active = true")
    int deactivateByUserIdAndDeviceId(@Param("userId") Long userId, @Param("deviceId") String deviceId);
}

