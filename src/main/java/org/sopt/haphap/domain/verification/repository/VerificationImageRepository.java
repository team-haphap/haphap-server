package org.sopt.haphap.domain.verification.repository;

import org.sopt.haphap.domain.verification.entity.VerificationImage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VerificationImageRepository extends JpaRepository<VerificationImage, Long> {

    // 여기서 고아 이미지란 - 등록에 연결되지 않은 채 threshold 이전에 만들어진 것
    @Query("""
        select v from VerificationImage v
        where v.registration is null
          and v.createdAt < :threshold
        order by v.createdAt asc
        """)
    List<VerificationImage> findOrphans(@Param("threshold") LocalDateTime threshold, Pageable pageable);
}