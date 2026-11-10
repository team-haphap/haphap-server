package org.sopt.haphap.domain.scrap.repository;

import java.time.LocalDateTime;
import org.sopt.haphap.domain.scrap.domain.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {

    // 조회 후 저장하면 동시 요청이 둘 다 "없음"을 보고 INSERT → 한쪽이 유니크 위반으로 실패한다.
    // 한 문장으로 원자적으로 처리하고, 반환값(1: 새로 찜함 / 0: 이미 찜해 있음)으로 결과를 구분한다.
    @Modifying
    @Query(value = """
        INSERT INTO scrap (user_id, posting_id, created_at, updated_at)
        VALUES (:userId, :postingId, :now, :now)
        ON CONFLICT (user_id, posting_id) DO NOTHING
        """, nativeQuery = true)
    int insertIfAbsent(@Param("userId") Long userId, @Param("postingId") Long postingId,
                       @Param("now") LocalDateTime now);

    // DELETE는 그 자체로 멱등이라 동시 요청이 와도 한쪽만 1, 나머지는 0을 받는다.
    @Modifying
    @Query("DELETE FROM Scrap s WHERE s.user.id = :userId AND s.posting.id = :postingId")
    int deleteByUserIdAndPostingId(@Param("userId") Long userId, @Param("postingId") Long postingId);

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM Scrap s WHERE s.user.id = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);
}
