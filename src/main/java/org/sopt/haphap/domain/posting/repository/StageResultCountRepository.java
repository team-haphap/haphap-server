package org.sopt.haphap.domain.posting.repository;

import org.sopt.haphap.domain.posting.domain.StageResultCount;
import org.sopt.haphap.domain.registration.dto.StageRegistrationCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StageResultCountRepository extends JpaRepository<StageResultCount, Long> {

    Optional<StageResultCount> findByPostingIdAndStageId(Long postingId, Long stageId);

    // 신규 등록: 해당 result 컬럼 +1 (row 있을 때만 반영, 없으면 0 반환 → 서비스에서 생성)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE StageResultCount c SET
              c.passCount    = c.passCount    + (CASE WHEN :result = 'PASS'    THEN 1 ELSE 0 END),
              c.failCount    = c.failCount    + (CASE WHEN :result = 'FAIL'    THEN 1 ELSE 0 END),
              c.pendingCount = c.pendingCount + (CASE WHEN :result = 'PENDING' THEN 1 ELSE 0 END),
              c.version      = c.version + 1
            WHERE c.postingId = :postingId AND c.stageId = :stageId
            """)
    int increment(@Param("postingId") Long postingId,
                  @Param("stageId") Long stageId,
                  @Param("result") String result);

    // PENDING → 확정 이동: pending -1, (pass 또는 fail) +1
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE StageResultCount c SET
              c.pendingCount = c.pendingCount - 1,
              c.passCount    = c.passCount + (CASE WHEN :newResult = 'PASS' THEN 1 ELSE 0 END),
              c.failCount    = c.failCount + (CASE WHEN :newResult = 'FAIL' THEN 1 ELSE 0 END),
              c.version      = c.version + 1
            WHERE c.postingId = :postingId AND c.stageId = :stageId
            """)
    int movePendingToConfirmed(@Param("postingId") Long postingId,
                               @Param("stageId") Long stageId,
                               @Param("newResult") String newResult);

    @Query("""
        SELECT c.postingId AS postingId, c.stageId AS stageId,
               (c.passCount + c.failCount ) AS cnt
        FROM StageResultCount c
        WHERE c.postingId IN :postingIds
        """)
    List<StageRegistrationCountProjection> findTotalsByPostingIds(@Param("postingIds") List<Long> postingIds);

    @Query("""
        SELECT (c.passCount + c.failCount)
        FROM StageResultCount c
        WHERE c.postingId = :postingId AND c.stageId = :stageId
        """)
    Long findConfirmedCount(@Param("postingId") Long postingId, @Param("stageId") Long stageId);

    // 전체 (posting,stage) 카운트 (PASS+FAIL)
    @Query("""
        SELECT c.postingId AS postingId, c.stageId AS stageId,
               (c.passCount + c.failCount) AS cnt
        FROM StageResultCount c
        """)
    List<StageRegistrationCountProjection> findAllTotals();
}