package org.sopt.haphap.domain.user.repository;

import jakarta.persistence.LockModeType;
import org.sopt.haphap.domain.user.entity.Provider;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.user.entity.WithdrawalStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    @Query("SELECT u.anonymousName FROM User u WHERE u.anonymousName IN :names")
    List<String> findAnonymousNamesIn(@Param("names") List<String> names);

    // 닉네임 중복 확인: 나를 뺀 활성 유저 중 같은 이름이 있는지 (uk_users_active_anonymous_name 부분 인덱스를 타도록 ACTIVE는 리터럴)
    @Query("""
        SELECT COUNT(u) > 0 FROM User u
        WHERE u.anonymousName = :anonymousName
          AND u.withdrawalStatus = org.sopt.haphap.domain.user.entity.WithdrawalStatus.ACTIVE
          AND u.id <> :userId
        """)
    boolean existsActiveAnonymousNameExcept(@Param("anonymousName") String anonymousName,
                                            @Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);

    @Query("""
    select u.id from User u
    where u.withdrawalStatus = :status and u.withdrawalRequestedAt < :before
    order by u.id asc
    """)
    List<Long> findIdsForUnlinkRetry(@Param("status") WithdrawalStatus status,
                                     @Param("before") LocalDateTime before,
                                     Pageable pageable);
}