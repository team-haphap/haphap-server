package org.sopt.haphap.domain.verification.repository;

import jakarta.persistence.LockModeType;
import org.sopt.haphap.domain.verification.entity.VerificationImage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface VerificationImageRepository extends JpaRepository<VerificationImage, Long> {

    @Query("""
    select v.id from VerificationImage v
    where v.registration is null
      and v.createdAt < :threshold
      and v.id > :lastId
    order by v.id asc
    """)
    List<Long> findOrphanIdsAfter(@Param("threshold") LocalDateTime threshold,
                                  @Param("lastId") Long lastId,
                                  Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from VerificationImage v where v.id = :id")
    Optional<VerificationImage> findByIdForUpdate(@Param("id") Long id);

    List<VerificationImage> findByRegistrationId(Long registrationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from VerificationImage v where v.id in :ids order by v.id asc")
    List<VerificationImage> findAllByIdInForUpdate(@Param("ids") Collection<Long> ids);

    @Modifying(flushAutomatically = true)
    @Query("""
    UPDATE VerificationImage v
    SET v.registration = null, v.sortOrder = null
    WHERE v.user.id = :userId AND v.registration IS NOT NULL
    """)
    int detachAllByUserId(@Param("userId") Long userId);

    @Query("""
    SELECT image
    FROM VerificationImage image
    JOIN FETCH image.registration registration
    WHERE registration.id IN :registrationIds
    ORDER BY registration.id ASC, image.sortOrder ASC, image.id ASC
    """)
    List<VerificationImage> findAttachedByRegistrationIds(
            @Param("registrationIds") Collection<Long> registrationIds
    );

    @Query("""
    SELECT image
    FROM VerificationImage image
    WHERE image.registration.id = :registrationId
    ORDER BY image.sortOrder ASC, image.id ASC
    """)
    List<VerificationImage> findAttachedByRegistrationIdOrdered(
            @Param("registrationId") Long registrationId
    );
}