package org.sopt.haphap.domain.registration.repository;

import jakarta.persistence.LockModeType;
import org.sopt.haphap.domain.registration.domain.Registration;
import org.sopt.haphap.domain.registration.domain.RegistrationResult;
import org.sopt.haphap.domain.registration.domain.RegistrationVerificationStatus;
import org.sopt.haphap.domain.registration.projection.*;

import org.springframework.data.domain.Page;
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

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    Optional<Registration> findByUserIdAndPostingIdAndStageId(Long userId, Long postingId, Long stageId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query ("select r from Registration r where r.id = :id")
    Optional<Registration> findByIdForUpdate (@Param("id") Long id);

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

    // 정합성 보정/재구성(StageResultCountReconciler, StageResultCountRebuilder)용 원본 재집계.
    // 실시간 집계(StageResultCountUpdater)가 보류하는 것과 반드시 같은 기준으로 걸러야 한다 —
    // 안 그러면 배치가 돌 때마다 보류해둔 값을 무시하고 원본으로 덮어써버린다.
    // PENDING은 항상 집계. PASS는 인증 승인된 것만. FAIL은 "후속 전형 불합격" 검토가 승인 안 된 건 제외.

    @Query("""
        SELECT r.posting.id AS postingId, r.stage.id AS stageId,
               r.result AS result, COUNT(r) AS cnt
        FROM Registration r
        WHERE r.result = org.sopt.haphap.domain.registration.domain.RegistrationResult.PENDING
           OR (r.result = org.sopt.haphap.domain.registration.domain.RegistrationResult.PASS
               AND r.verificationStatus = org.sopt.haphap.domain.registration.domain.RegistrationVerificationStatus.APPROVED)
           OR (r.result = org.sopt.haphap.domain.registration.domain.RegistrationResult.FAIL
               AND NOT EXISTS (
                   SELECT 1 FROM RegistrationReview rv
                   WHERE rv.registration = r
                     AND rv.reason = org.sopt.haphap.domain.registration.domain.RegistrationReviewReason.SUBSEQUENT_STAGE_FAIL
                     AND rv.status <> org.sopt.haphap.domain.registration.domain.RegistrationReviewStatus.ACCEPTED
               ))
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
    SELECT u.id AS userId,
           u.profileImageUrl AS profileImageUrl,
           MAX(r.updatedAt) AS lastActivity
    FROM Registration r
    JOIN r.user u
    WHERE r.posting.id = :postingId
      AND u.withdrawalStatus = org.sopt.haphap.domain.user.entity.WithdrawalStatus.ACTIVE
    GROUP BY u.id, u.profileImageUrl
    ORDER BY MAX(r.updatedAt) DESC
    """)
    List<RecentParticipantProjection> findRecentParticipants(
            @Param("postingId") Long postingId, Pageable pageable);


    // clearAutomatically 금지: start()에서 잠근 User가 detach되면 탈퇴 상태 변경이 저장되지 않음
    @Modifying(flushAutomatically = true)
    @Query("""
    UPDATE Registration r
    SET r.contactMethods = null,
        r.contactedAt = null,
        r.anonymous = true,
        r.verificationStatus = CASE
            WHEN r.verificationStatus = org.sopt.haphap.domain.registration.domain.RegistrationVerificationStatus.PENDING
            THEN org.sopt.haphap.domain.registration.domain.RegistrationVerificationStatus.REJECTED
            ELSE r.verificationStatus END
    WHERE r.user.id = :userId
    """)
    int anonymizeAllByUserId(@Param("userId") Long userId);

    // 실시간 제보 30개 (registrations)( updatedAt 최신순, 수정 포함.)
    @Query("""
    SELECT r.id AS registrationId, s.name AS stage, u.anonymousName AS nickName, r.result AS status, r.updatedAt AS feedCreatedAt
    FROM Registration r
    JOIN r.stage s
    JOIN r.user u
    WHERE r.posting.id = :postingId
      AND u.withdrawalStatus = org.sopt.haphap.domain.user.entity.WithdrawalStatus.ACTIVE
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

    // 운영진 검토 트리거: 오늘(자정~) 이 (공고,전형)에 등록된 불합격 건수
    @Query("""
        SELECT COUNT(r)
        FROM Registration r
        WHERE r.posting.id = :postingId AND r.stage.id = :stageId
          AND r.result = org.sopt.haphap.domain.registration.domain.RegistrationResult.FAIL
          AND r.updatedAt >= :startOfDay AND r.updatedAt < :startOfTomorrow
        """)
    long countFailToday(@Param("postingId") Long postingId, @Param("stageId") Long stageId,
                        @Param("startOfDay") LocalDateTime startOfDay,
                        @Param("startOfTomorrow") LocalDateTime startOfTomorrow);

    // 운영진 검토 트리거: 이 (공고,전형)에 승인된 합격이 이미 있는지 ("합격 인증 없이" 조건)
    @Query("""
        SELECT COUNT(r) > 0
        FROM Registration r
        WHERE r.posting.id = :postingId AND r.stage.id = :stageId
          AND r.result = org.sopt.haphap.domain.registration.domain.RegistrationResult.PASS
          AND r.verificationStatus = org.sopt.haphap.domain.registration.domain.RegistrationVerificationStatus.APPROVED
        """)
    boolean existsApprovedPass(@Param("postingId") Long postingId, @Param("stageId") Long stageId);

    // 홈 [내 지원]: 이 유저가 "유효한" 결과를 등록한 공고 id를, 그 공고에 마지막으로 활동한 시각(=최근 상태 등록) 최신순으로.
    // 합격은 운영진 승인 전까지는 등록한 결과로 안 침 (승인되는 순간 approve()가 updatedAt을 갱신해서 그때부터 반영됨).
    @Query("""
        SELECT r.posting.id
        FROM Registration r
        WHERE r.user.id = :userId
          AND (r.result <> org.sopt.haphap.domain.registration.domain.RegistrationResult.PASS
               OR r.verificationStatus = org.sopt.haphap.domain.registration.domain.RegistrationVerificationStatus.APPROVED)
        GROUP BY r.posting.id
        ORDER BY MAX(r.updatedAt) DESC
        """)
    List<Long> findAppliedPostingIdsOrderByLastActivity(@Param("userId") Long userId);

    // 앱 실행 시 자동 노출용
    @Query("""
        SELECT r FROM Registration r
        JOIN FETCH r.user
        JOIN FETCH r.posting p
        JOIN FETCH p.company
        JOIN FETCH p.category
        WHERE r.user.id = :userId
          AND r.verificationStatus = :status
          AND r.passCardConfirmedAt IS NULL
        ORDER BY r.id ASC
        """)
    List<Registration> findUnconfirmedPassCards(
            @Param("userId") Long userId,
            @Param("status") RegistrationVerificationStatus status);

    // 보관함; 확인 완료한 합격카드
    @Query("""
        SELECT r FROM Registration r
        JOIN FETCH r.user
        JOIN FETCH r.posting p
        JOIN FETCH p.company
        JOIN FETCH p.category
        WHERE r.user.id = :userId
          AND r.verificationStatus = :status
          AND r.passCardConfirmedAt IS NOT NULL
        ORDER BY r.passCardConfirmedAt DESC, r.id DESC
        """)
    Page<Registration> findConfirmedPassCards(
            @Param("userId") Long userId,
            @Param("status") RegistrationVerificationStatus status,
            Pageable pageable);

    // 상세 조회
    @Query("""
        SELECT r FROM Registration r
        JOIN FETCH r.user
        JOIN FETCH r.posting p
        JOIN FETCH p.company
        JOIN FETCH p.category
        WHERE r.id = :registrationId
          AND r.user.id = :userId
          AND r.verificationStatus = :status
        """)
    Optional<Registration> findPassCard(
            @Param("registrationId") Long registrationId,
            @Param("userId") Long userId,
            @Param("status") RegistrationVerificationStatus status);

    //재제출 API
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Registration r where r.id = :id and r.user.id = :userId")
    Optional<Registration> findByIdAndUserIdForUpdate(@Param("id") Long id, @Param("userId") Long userId);

    // 목록 + 기업/전형까지 fetch join
    @Query("""
    SELECT r FROM Registration r
    JOIN FETCH r.posting p
    JOIN FETCH p.company
    JOIN FETCH r.stage
    WHERE r.user.id = :userId
      AND r.verificationStatus IN :statuses
    ORDER BY r.verificationResolvedAt DESC, r.id DESC
    """)
    List<Registration> findVerificationHistory(
            @Param("userId") Long userId,
            @Param("statuses") Collection<RegistrationVerificationStatus> statuses);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    UPDATE Registration r
       SET r.passCardConfirmedAt = :now
     WHERE r.id = :id
       AND r.user.id = :userId
       AND r.verificationStatus = :status
       AND r.passCardConfirmedAt IS NULL
    """)
    int markPassCardConfirmed(@Param("id") Long id,
                              @Param("userId") Long userId,
                              @Param("status") RegistrationVerificationStatus status,
                              @Param("now") LocalDateTime now);

    boolean existsByIdAndUser_IdAndVerificationStatus(
            Long id, Long userId, RegistrationVerificationStatus status);
    // 마이페이지 [합격 인증]: 내 합격 등록 중 승인/반려 건수. 승인 대기(PENDING)는 어느 쪽에도 안 셈.
    // 집계 쿼리라 항상 한 행이 나온다 (등록이 없으면 0, 0). user_id 선두 유니크 인덱스를 탄다.
    @Query("""
        SELECT COALESCE(SUM(CASE WHEN r.verificationStatus = org.sopt.haphap.domain.registration.domain.RegistrationVerificationStatus.APPROVED
                                 THEN 1 ELSE 0 END), 0) AS approvedCount,
               COALESCE(SUM(CASE WHEN r.verificationStatus = org.sopt.haphap.domain.registration.domain.RegistrationVerificationStatus.REJECTED
                                 THEN 1 ELSE 0 END), 0) AS rejectedCount
        FROM Registration r
        WHERE r.user.id = :userId
          AND r.result = org.sopt.haphap.domain.registration.domain.RegistrationResult.PASS
        """)
    PassCertificationCountProjection countPassCertifications(@Param("userId") Long userId);

    // 마이페이지 [내 지원 내역]: 위와 같은 "유효한 결과" 기준에 마지막 활동 시각까지 함께. 개수는 Pageable로 자른다.
    // 같은 시각이면 공고 id 역순으로 고정해 페이지 경계에서 순서가 흔들리지 않게 한다.
    @Query("""
        SELECT r.posting.id AS postingId, MAX(r.updatedAt) AS lastActivityAt
        FROM Registration r
        WHERE r.user.id = :userId
          AND (r.result <> org.sopt.haphap.domain.registration.domain.RegistrationResult.PASS
               OR r.verificationStatus = org.sopt.haphap.domain.registration.domain.RegistrationVerificationStatus.APPROVED)
        GROUP BY r.posting.id
        ORDER BY MAX(r.updatedAt) DESC, r.posting.id DESC
        """)
    List<AppliedPostingProjection> findAppliedPostingsOrderByLastActivity(
            @Param("userId") Long userId, Pageable pageable);

    // 마이페이지 [내 지원 내역] 전체보기: 같은 "유효한 결과" 기준으로 유저의 등록을 전형 정보와 함께 전부.
    // 유저 한 명의 등록 수는 작아서 한 번에 읽고, 공고별 그룹핑/정렬은 서비스에서 한다 (쿼리 1번으로 끝).
    @Query("""
        SELECT r.posting.id AS postingId, s.name AS stageName, s.orderIndex AS stageOrder,
               r.result AS result, r.updatedAt AS updatedAt
        FROM Registration r
        JOIN r.stage s
        WHERE r.user.id = :userId
          AND (r.result <> org.sopt.haphap.domain.registration.domain.RegistrationResult.PASS
               OR r.verificationStatus = org.sopt.haphap.domain.registration.domain.RegistrationVerificationStatus.APPROVED)
        """)
    List<MyRegistrationRowProjection> findValidRegistrationRowsByUserId(@Param("userId") Long userId);
}
