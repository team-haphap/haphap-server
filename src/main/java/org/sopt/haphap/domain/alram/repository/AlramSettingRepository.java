package org.sopt.haphap.domain.alram.repository;

import org.sopt.haphap.domain.alram.domain.AlramSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlramSettingRepository extends JpaRepository<AlramSetting, Long> {

    Optional<AlramSetting> findByUserIdAndPostingId(Long userId, Long postingId);

    // 합격/마감 알림 모두 등록자 본인 포함 전체 구독자에게 발송한다(제외 없음).
    @Query("""
        select s from AlramSetting s
        join fetch s.user
        where s.posting.id = :postingId
          and s.enabled = true
    """)
    List<AlramSetting> findActiveSubscribers(@Param("postingId") Long postingId);

    @Modifying(flushAutomatically = true)
    @Query("delete from AlramSetting s where s.user.id = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);
}