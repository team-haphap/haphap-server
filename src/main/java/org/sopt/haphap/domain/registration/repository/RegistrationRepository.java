package org.sopt.haphap.domain.registration.repository;

import org.sopt.haphap.domain.registration.domain.Registration;
import org.sopt.haphap.domain.registration.domain.RegistrationResult;
import org.sopt.haphap.domain.registration.dto.StageRegistrationCountProjection;
import org.sopt.haphap.domain.registration.dto.StageResultAggProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    Optional<Registration> findByUserIdAndPostingIdAndStageId(Long userId, Long postingId, Long stageId);

    //(공고, 전형)별 등록 수를 한 번에
    @Query("""
        SELECT r.posting.id AS postingId, r.stage.id AS stageId, COUNT(r) AS cnt
        FROM Registration r
        WHERE r.posting.id IN :postingIds
        GROUP BY r.posting.id, r.stage.id
        """)
    List<StageRegistrationCountProjection> countByPostingAndStage(
            @Param("postingIds") List<Long> postingIds);

    //48시간 내 PASS/FAIL 결과가 있는 공고 id 추리기.
    @Query("""
        SELECT DISTINCT r.posting.id
        FROM Registration r
        WHERE r.result IN :results
          AND r.updatedAt >= :since
          AND (:categoryNames IS NULL OR r.posting.category.name IN :categoryNames)
        """)
    List<Long> findRecentlyActivePostingIds(
            @Param("results") List<RegistrationResult> results,
            @Param("since") LocalDateTime since,
            @Param("categoryNames") List<String> categoryNames);

    //인기 정렬
    @Query("""
        SELECT r.posting.id AS postingId, r.stage.id AS stageId, COUNT(r) AS cnt
        FROM Registration r
        WHERE r.result IN :results
          AND r.updatedAt >= :since
          AND r.posting.id IN :postingIds
        GROUP BY r.posting.id, r.stage.id
        """)
    List<StageRegistrationCountProjection> countRecentActiveByPostingAndStage(
            @Param("results") List<RegistrationResult> results,
            @Param("since") LocalDateTime since,
            @Param("postingIds") List<Long> postingIds);

    // 테스트용
    @Query("""
        SELECT r.posting.id AS postingId, r.stage.id AS stageId,
               r.result AS result, COUNT(r) AS cnt
        FROM Registration r
        GROUP BY r.posting.id, r.stage.id, r.result
        """)
    List<StageResultAggProjection> aggregateAllForRebuild();

    @Query("""
    select r from Registration r
    join fetch r.user
    join fetch r.stage
    join fetch r.posting p
    join fetch p.company
    join fetch p.category
    where r.id = :id
    """)
    Optional<Registration> findByIdWithDetails(@Param("id") Long id);
}