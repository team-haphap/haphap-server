package org.sopt.haphap.domain.posting.repository;

import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageFlatProjection;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageCalendarProjection;
import org.sopt.haphap.domain.posting.dto.response.PostingStageResponse;
import org.sopt.haphap.domain.posting.dto.projection.TodayAnnouncementProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.sopt.haphap.domain.posting.domain.CompanyImageType;

import java.time.LocalDate;
import java.util.List;

public interface PostingStageRepository extends JpaRepository<PostingStage, Long> {
    @Query("""
            SELECT new org.sopt.haphap.domain.posting.dto.response.PostingStageResponse(s.id, s.name, s.orderIndex)
            FROM PostingStage s
            WHERE s.posting.id = :postingId
            ORDER BY s.orderIndex ASC
            """)
    List<PostingStageResponse> findStagesByPostingId(@Param("postingId") Long postingId);

    @Query("""
        SELECT ps.posting.id AS postingId,
               ps.id AS stageId,
               ps.name AS name,
               ps.orderIndex AS orderIndex,
               ps.expectedAnnouncementDate AS expectedAnnouncementDate,
               ps.announcedDate AS announcedDate
        FROM PostingStage ps
        WHERE ps.posting.id IN :postingIds
        ORDER BY ps.posting.id, ps.orderIndex
        """)
    List<PostingStageFlatProjection> findFlatByPostingIds(@Param("postingIds") List<Long> postingIds);

    @Query("""
    SELECT s.id AS stageId, s.name AS stageName, s.expectedScore AS expectedScore,
           p.id AS postingId, p.title AS title,
           c.name AS companyName, cat.name AS categoryName,
           ci.imageUrl AS imageUrl
    FROM PostingStage s
    JOIN s.posting p
    JOIN p.company c
    JOIN p.category cat
    LEFT JOIN CompanyImage ci ON ci.company = c AND ci.type = :imageType
    WHERE s.expectedAnnouncementDate = :today
    ORDER BY s.expectedScore DESC, p.title ASC
    """)
    List<TodayAnnouncementProjection> findTodayAnnouncements(
            @Param("today") LocalDate today,
            @Param("imageType") CompanyImageType imageType,
            Pageable pageable);

    @Query("""
    SELECT s.posting.id AS postingId, s.id AS stageId,
           s.name AS stageName, s.expectedScore AS expectedScore,
           s.expectedAnnouncementDate AS expectedAnnouncementDate,
           p.title AS title, ci.imageUrl AS logoImageUrl,
           s.orderIndex AS orderIndex
    FROM PostingStage s
    JOIN s.posting p
    JOIN p.company c
    LEFT JOIN CompanyImage ci ON ci.company = c AND ci.type = :imageType
    WHERE s.expectedAnnouncementDate = :date
    """)
    List<PostingStageCalendarProjection> findCalendarStagesByDate(
            @Param("date") LocalDate date,
            @Param("imageType") CompanyImageType imageType);

    // 월별 인디케이터 조회 전용
    @Query("""
    SELECT s.posting.id AS postingId, s.id AS stageId,
           s.name AS stageName, s.expectedScore AS expectedScore,
           s.expectedAnnouncementDate AS expectedAnnouncementDate
    FROM PostingStage s
    WHERE s.expectedAnnouncementDate BETWEEN :start AND :end
    """)
    List<PostingStageCalendarProjection> findCalendarStagesByDateRange(
            @Param("start") LocalDate start, @Param("end") LocalDate end);
    //announcedCount — 오늘 발표 감지된 전형이 있는 공고 수
    @Query("""
        SELECT COUNT(DISTINCT s.posting.id)
        FROM PostingStage s
        WHERE s.announcedDate = :today
        """)
    long countPostingsAnnouncedToday(@Param("today") LocalDate today);

    // 전체 전형 (공고별 그룹핑용)
    @Query("""
        SELECT s.posting.id AS postingId, s.id AS stageId,
               s.name AS name, s.orderIndex AS orderIndex,
               s.expectedAnnouncementDate AS expectedAnnouncementDate
        FROM PostingStage s
        ORDER BY s.posting.id ASC, s.orderIndex ASC
        """)
    List<PostingStageFlatProjection> findAllStages();
    boolean existsByPostingIdAndOrderIndex(Long postingId, int orderIndex);
}
