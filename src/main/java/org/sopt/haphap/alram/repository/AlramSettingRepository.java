package org.sopt.haphap.alram.repository;

import org.sopt.haphap.alram.domain.AlramSetting;
import org.sopt.haphap.posting.domain.Posting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlramSettingRepository extends JpaRepository<AlramSetting, Long> {

    Optional<AlramSetting> findByUserIdAndPostingId(Long userId, Long postingId);

    @Query("""
        select s from AlramSetting s
        join fetch s.user
        where s.posting.id = :postingId
          and s.enabled = true
          and s.user.id <> :registrantId
    """)
    List<AlramSetting> findActiveSubscribers(@Param("postingId") Long postingId,
                                             @Param("registrantId") Long registrantId);
}