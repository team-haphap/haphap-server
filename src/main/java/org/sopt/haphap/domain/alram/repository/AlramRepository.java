package org.sopt.haphap.domain.alram.repository;

import org.sopt.haphap.domain.alram.domain.Alram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlramRepository extends JpaRepository<Alram, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Alram p where a.user.id = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);
}