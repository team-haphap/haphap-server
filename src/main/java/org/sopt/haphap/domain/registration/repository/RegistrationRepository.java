package org.sopt.haphap.domain.registration.repository;

import org.sopt.haphap.domain.registration.domain.Registration;
import org.sopt.haphap.domain.registration.domain.RegistrationResult;
import org.sopt.haphap.domain.registration.projection.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    Optional<Registration> findByUserIdAndPostingIdAndStageId(Long userId, Long postingId, Long stageId);

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

    // distinct 유저 수 (registeredCount)
    @Query("""
        SELECT COUNT(DISTINCT r.user.id)
        FROM Registration r
        WHERE r.posting.id = :postingId
        """)
    long countDistinctUsersByPostingId(@Param("postingId") Long postingId);

    // 최근 등록 유저 4명 (profileImages용)( updatedAt 최신순, 중복 유저 제거. distinct + 정렬이 얽혀서 프로젝션으로.)
    @Query("""
        SELECT r.user.id AS userId,
               r.user.profileImageUrl AS profileImageUrl,
               MAX(r.updatedAt) AS lastActivity
        FROM Registration r
        WHERE r.posting.id = :postingId
        GROUP BY r.user.id, r.user.profileImageUrl
        ORDER BY MAX(r.updatedAt) DESC
        """)
    List<RecentParticipantProjection> findRecentParticipants(
            @Param("postingId") Long postingId, Pageable pageable);

    // 실시간 제보 30개 (registrations)( updatedAt 최신순, 수정 포함.)
    @Query("""
        SELECT r.id AS registrationId, s.name AS stage, u.anonymousName AS nickName,r.result AS status, r.updatedAt AS feedCreatedAt
        FROM Registration r
        JOIN r.stage s
        JOIN r.user u
        WHERE r.posting.id = :postingId
        ORDER BY r.updatedAt DESC
        """)
    List<RegistrationFeedProjection> findRecentFeeds(@Param("postingId") Long postingId, Pageable pageable);

    // 캘린더 참여인원: 공고의 모든 전형 통틀어 상태 등록한 유저 수 (result 무관, 중복 제거)
    @Query("""
    SELECT r.posting.id AS postingId, COUNT(DISTINCT r.user.id) AS cnt
    FROM Registration r
    WHERE r.posting.id IN :postingIds
    GROUP BY r.posting.id
    """)
    List<PostingParticipantCountProjection> countDistinctUsersByPostingIds(
            @Param("postingIds") List<Long> postingIds);

    //cumulatedCount
    @Query("""
        SELECT
          SUM(CASE WHEN r.createdAt >= :startOfDay AND r.createdAt < :startOfTomorrow
                   THEN 1 ELSE 0 END)
          +
          SUM(CASE WHEN r.updatedAt >= :startOfDay AND r.updatedAt < :startOfTomorrow
                        AND r.updatedAt <> r.createdAt
                   THEN 1 ELSE 0 END)
        FROM Registration r
        WHERE (r.createdAt >= :startOfDay AND r.createdAt < :startOfTomorrow)
           OR (r.updatedAt >= :startOfDay AND r.updatedAt < :startOfTomorrow)
        """)
    Long countTodayEvents(@Param("startOfDay") LocalDateTime startOfDay,
                          @Param("startOfTomorrow") LocalDateTime startOfTomorrow);
}